package net.fast2smart.streaming

import java.lang.Long
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import java.util.Date
import java.util.concurrent.{Executors, TimeUnit}

import com.datastax.spark.connector.rdd.CassandraLeftJoinRDD
import com.datastax.spark.connector.types.TypeConverter
import com.datastax.spark.connector.{CassandraRow, SomeColumns, _}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{LongDeserializer, LongSerializer, StringDeserializer, StringSerializer}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization.write

import scala.collection.mutable
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.typeTag
import scala.util.{Failure, Success}

/**
  * Created by markus on 06/11/2016.
  */
object PurchaseStreaming {
  val jobName: String = "PurchaseStreaming"

  def main(args: Array[String]): Unit = {


    val spark = SparkSession
      .builder
      .appName(jobName)
      .config("spark.streaming.stopGracefullyOnShutdown", "true")
      .config("spark.streaming.kafka.maxRatePerPartition", "800")
      .config("spark.executor.instances", "3")
      //      .config("spark.shuffle.service.enabled", "true")
      //      .config("spark.dynamicAllocation.enabled", "true")
      //      .config("spark.dynamicAllocation.minExecutors", "2")
      //      .config("spark.dynamicAllocation.maxExecutors", "4")
      .config("spark.cassandra.connection.host", "cassandra-1,cassandra-2,cassandra-3")
      .getOrCreate()

    //create streaming context with a window size of 1s, so every second it evaluates new data from underlying stream
    val ssc = new StreamingContext(spark.sparkContext, Seconds(1))

    //Shutdown Hook for YARN
    Executors
      .newScheduledThreadPool(1)
      .scheduleWithFixedDelay(new Runnable {
        val fs: FileSystem = FileSystem.get(new Configuration())

        override def run(): Unit = {
          if (!fs.exists(new Path(s"/tmp/$jobName.running"))) {
            ssc.stop(stopSparkContext = true, stopGracefully = true)
          }
        }
      }, 5, 5, TimeUnit.SECONDS)

    //create KafkaStream for topic purchases
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "kafka-1:9092,kafka-2:9092,kafka-3:9092",
      "key.deserializer" -> classOf[LongDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "purchase-streaming-spark"
    )

    val kafkaStream = KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, Subscribe[String, String](Seq("purchases"), kafkaParams))

    implicit val localDateTimeTypeConverter: TypeConverter[LocalDateTime] = new TypeConverter[LocalDateTime] {
      def targetTypeTag: universe.TypeTag[LocalDateTime] = typeTag[LocalDateTime]

      def convertPF: PartialFunction[Any, LocalDateTime] = {
        case x: String => LocalDateTime.parse(x, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        case x: Date => LocalDateTime.ofInstant(x.toInstant, ZoneId.systemDefault())
      }
    }

    kafkaStream.foreachRDD { rdd =>
      //get the offsets to commit afterwards if work is done
      val offsets = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      //Join current known account balance from batch process to have cut-off maxdate
      val joinWithCassandraTable: CassandraLeftJoinRDD[(Long, Int, Int, Purchase), CassandraRow] = rdd
        .map(record => {
          implicit val formats: Formats = DefaultFormats + new LocalDateTimeSerializer
          parse(record.value()).extract[Purchase]
        })
        .map(purchase => {
          (purchase.member.id, purchase.date.getYear, purchase.date.getMonthValue, purchase)
        })
        .leftJoinWithCassandraTable("fast2smart", "member_monthly_balance", joinColumns = SomeColumns("member", "year", "month"))

      //Calculate new delta because of purchase and cut-off maxdate
      val accountBalanceWithNewDelta: RDD[(Long, Int, Int, Purchase, Long, Long, LocalDateTime)] = joinWithCassandraTable
        .map {
          case ((memberId, year, month, purchase), None) =>
            (memberId, year, month, purchase, 0L, purchase.total(), purchase.date)
          case ((memberId, year, month, purchase), Some(account)) if purchase.date.isAfter(account.get[LocalDateTime]("maxdate")) =>
            (memberId, year, month, purchase, account.get[Long]("amount"), purchase.total(), purchase.date)
          case ((memberId, year, month, purchase), Some(account)) =>
            (memberId, year, month, purchase, account.get[Long]("amount"), 0L, account.get[LocalDateTime]("maxdate"))
        }

      //Join with previously known delta from database and calculate new according to maxdate
      val accountBalancWithDeltas: RDD[((Long, Int, Int, Purchase), Long, Long, LocalDateTime)] = accountBalanceWithNewDelta
        .leftJoinWithCassandraTable("fast2smart", "member_delta_balance", joinColumns = SomeColumns("member", "year", "month"))
        .map {
          case ((memberId, year, month, purchase, balance, delta, maxdate), None) =>
            ((memberId, year, month, purchase), balance, delta, maxdate)
          case ((memberId, year, month, purchase, balance, delta, maxdate), Some(previousDelta)) if delta > 0L && maxdate.isAfter(previousDelta.get[LocalDateTime]("maxdate")) =>
            ((memberId, year, month, purchase), balance, previousDelta.get[Long]("amount") + delta, maxdate)
          case ((memberId, year, month, purchase, balance, delta, maxdate), Some(previousDelta)) =>
            ((memberId, year, month, purchase), balance, previousDelta.get[Long]("amount"), previousDelta.get[LocalDateTime]("maxdate"))
        }

      //Save current delta with TTL of 48h hours, assumption is that regulary the montly balance is recalculated
      import spark.implicits._
      accountBalancWithDeltas
        .filter {
          case ((_, _, _, _), _, delta, _) => delta > 0L
        }
        .map {
          case ((memberId, year, month, _), _, delta, maxdate) => DeltaMonthlyBalance(memberId, year, month, delta, Timestamp.valueOf(maxdate))
        }
        .toDF()
        .write
        .format("org.apache.spark.sql.cassandra")
        .options(Map("keyspace" -> "fast2smart", "table" -> "member_delta_balance", "output.ttl" -> "172800"))
        .mode(SaveMode.Append)
        .save()

      //And now we have a tuple of Purchase Event and correct balance for this month, to fulfill our campaign: customer with at least X points this month ...
      accountBalancWithDeltas
        .map {
          //year >= 2015 is just for beeing compatible to Gatling simulations
          case ((memberId, year, _, purchase), balance, delta, _) if delta + balance > 199 && year >= 2015 => Success(TreatmentAssignment(Member(memberId), purchase.partner, "5x Points Booster"))
          case ((_, _, _, purchase), _, _, _) => Failure(new Exception("less points"))
        }
        .filter(_.isSuccess)
        .foreachPartition(
          records => {
            //send each to another kafka topic
            val producer = KafkaProducerFactory.getOrCreateProducer(Map("bootstrap.servers" -> "192.168.10.5:9092,192.168.10.6:9092,192.168.10.7:9092"))
            implicit val formats: Formats = DefaultFormats + new LocalDateTimeSerializer + new MemberSerializer
            records.foreach(record =>
              producer.send(new ProducerRecord[Long, String]("treatments", record.get.member.id, write(record.get)))
            )
          }
        )
      //now work is successfully done, let Kafka Broker know about new offsets
      kafkaStream.asInstanceOf[CanCommitOffsets].commitAsync(offsets)
    }

    ssc.start()
    ssc.awaitTermination()
  }
}

case class TreatmentAssignment(member: Member, partner: String, headline: String)

case class Member(id: Long)

class MemberSerializer extends CustomSerializer[Member](format => ( {
  case (JString(l)) => Member(Long.parseLong(l))
}, {
  case m: Member => JString(m.id.toString)
}))

case class Purchase(member: Member, partner: String, amount: BigDecimal, currency: String = "EUR", basePoints: Long, statusPoints: Long = 0L, date: LocalDateTime) {
  def total(): Long = {
    basePoints + statusPoints
  }
}

case class DeltaMonthlyBalance(member: Long, year: Int, month: Int, amount: Long, maxdate: Timestamp)

class LocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format => ( {
  case JString(s) =>
    LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}, {
  case x: LocalDateTime =>
    JString(x.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
}))

object KafkaProducerFactory {

  import scala.collection.JavaConversions._

  private val producers = mutable.Map[Map[String, Object], KafkaProducer[Long, String]]()

  def getOrCreateProducer(config: Map[String, Object]): KafkaProducer[Long, String] = {

    val defaultConfig: Map[String, Object] = Map(
      "key.serializer" -> classOf[LongSerializer],
      "value.serializer" -> classOf[StringSerializer]
    )

    val finalConfig = defaultConfig ++ config

    producers.getOrElseUpdate(finalConfig, {
      val producer = new KafkaProducer[Long, String](finalConfig)

      sys.addShutdownHook {
        producer.close()
      }
      producer
    })
  }
}