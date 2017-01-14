package net.fast2smart.batch

import java.sql.Timestamp
import java.time.LocalDateTime

import org.apache.spark.sql.{SaveMode, SparkSession, _}

/**
  * Created by markus on 06/11/2016.
  */
object AccountAggregation {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder
      .appName("Account Aggregation")
      .master("local[2]")
      .config("spark.cassandra.connection.host", "192.168.10.8,192.168.10.9,192.168.10.10")
      .getOrCreate()

    import spark.implicits._

    //load complete purchase table
    val purchases: DataFrame = spark.read
      .format("jdbc")
      .option("url", "jdbc:h2:../db/fast2smart;AUTO_SERVER=TRUE")
      .option("dbtable", "purchase")
      .option("user", "sa")
      .load()

    purchases.show(10, truncate = false)

    //group by relevant keys and aggregate points
    val aggregatedAccounts: DataFrame = purchases
      .map(row => {
        val timestamp: Timestamp = row.getAs[Timestamp]("DATE")
        val dateTime: LocalDateTime = timestamp.toLocalDateTime
        Account(row.getAs[Long]("MEMBER_ID"), row.getAs[Long]("BASE_POINTS") + row.getAs[Long]("STATUS_POINTS"), dateTime.getYear, dateTime.getMonthValue, timestamp)
      })
      .groupBy("member", "year", "month").agg(Map("points" -> "sum", "date" -> "max"))
      .withColumnRenamed("sum(points)", "amount") //SparkSQL will deliver function(colum) as new column name
      .withColumnRenamed("max(date)", "maxdate")

    aggregatedAccounts.show(10, truncate = false)

    //and now save to Cassandra, if it will map column names automatically
    aggregatedAccounts
      .write
      .mode(SaveMode.Append) //this ensure that there is always data and get's overwritten
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "member_monthly_balance", "keyspace" -> "fast2smart"))
      .save()
  }
}

case class Account(member: Long, points: Long, year: Int, month: Int, date: Timestamp)
