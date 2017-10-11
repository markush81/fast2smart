package net.fast2smart.streaming

import java.util.Properties

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

object PurchaseStreaming {
  def main(args: Array[String]) {

    // set up the execution environment
    val streamingEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val kafkaParams = new Properties()
    kafkaParams.setProperty("bootstrap.servers", "kafka-1:9092,kafka-2:9092,kafka-3:9092")
    kafkaParams.setProperty("group.id", "purchase-streaming-flink")

    val kafkaSource = new FlinkKafkaConsumer010[String]("purchases", new SimpleStringSchema(), kafkaParams)
    kafkaSource.setStartFromEarliest()

    streamingEnvironment
      .addSource(kafkaSource)
      .print()

    streamingEnvironment.execute("PurchaseStreaming")
  }
}