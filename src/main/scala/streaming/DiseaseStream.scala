package streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

/**
 * Disease Stream
 * ----------------------------------
 * This file reads disease events from Kafka using Spark
 * Structured Streaming, parses JSON messages, and outputs
 * the structured data in real time.
 */

object DiseaseStream {
  // Kafka broker address
  val KAFKA_BOOTSTRAP = "localhost:9092"
  // Kafka topic that contains disease events
  val TOPIC = "disease_topic"

  // Checkpoint directory for Spark Structured Streaming
  // Used to store streaming metadata and ensure fault tolerance
  val CHECKPOINT_DIR =
    "file:///C:/weather-diseasebigdata/data/checkpoints/Streaming/diseaseStream"

      // Schema that matches the JSON structure sent by DiseaseProducer
      val diseaseSchema: StructType = new StructType()
    .add("disease", StringType, nullable = true)
    .add("country", StringType, nullable = true)
    .add("infection_rate", DoubleType, nullable = true)
    .add("event_time", StringType, nullable = true)

  def main(args: Array[String]): Unit = {
    // Create SparkSession for Structured Streaming
    val spark = SparkSession.builder()
      .appName("DiseaseStream")
      .master("local[*]")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.streaming.forceDeleteTempCheckpointLocation", "true")
      .getOrCreate()
    // Reduce log verbosity for cleaner output
    spark.sparkContext.setLogLevel("WARN")


    // Read streaming data from Kafka
    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP)
      .option("subscribe", TOPIC)
      // Read only new incoming messages
      .option("startingOffsets", "latest")
      .load()


    // Cast Kafka key and value to String
    val raw = kafkaDF
      .selectExpr(
        "CAST(key AS STRING) as kafka_key",
        "CAST(value AS STRING) as value"
      )
      // Filter out empty or invalid messages
      .filter(length(trim(col("value"))) > 0)


    // Parse JSON payload into structured columns
    val parsed = raw
      .withColumn("json", from_json(col("value"), diseaseSchema))
      // Drop records that failed JSON parsing
      .filter(col("json").isNotNull)
      .select(
        col("kafka_key"),
        col("json.disease").as("disease"),
        col("json.country").as("country"),
        col("json.infection_rate").as("infection_rate"),
        col("json.event_time").as("event_time")
      )
      //this to ensure country field is not empty
      .filter(length(trim(col("country"))) > 0)


    // Write streaming output to console

    val query = parsed
      .select("kafka_key", "event_time", "disease", "country", "infection_rate")
      .writeStream
      .format("console")
      .outputMode("append")
      .option("truncate", "false")
      .option("checkpointLocation", CHECKPOINT_DIR)
      .start()

    // Keep the streaming query running
    query.awaitTermination()
  }
}
