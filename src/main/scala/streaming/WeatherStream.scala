import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object weatherStream {

  val KAFKA_BOOTSTRAP = "localhost:9092"
  val TOPIC = "weather_topic"
  val CHECKPOINT_DIR =
    "file:///D:/BD_Project/weather-diseasebigdata/checkpoint/weatherStream"

  val weatherSchema: StructType = new StructType()
    .add("country", StringType, nullable = true)
    .add("temperature", DoubleType, nullable = true)
    .add("humidity", DoubleType, nullable = true)

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("WeatherStream")
      .master("local[*]")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.streaming.forceDeleteTempCheckpointLocation", "true")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP)
      .option("subscribe", TOPIC)
      .option("startingOffsets", "latest")
      .load()

    val raw = kafkaDF
      .selectExpr(
        "CAST(key AS STRING) as key",
        "CAST(value AS STRING) as value"
      )
      .filter(length(trim(col("value"))) > 0)

    val parsed = raw
      .withColumn("json", from_json(col("value"), weatherSchema))
      .filter(col("json").isNotNull)
      .select(
        col("key"),
        col("json.country").as("country"),
        col("json.temperature").as("temperature"),
        col("json.humidity").as("humidity")
      )
      .withColumn("event_time", current_timestamp())

    val query = parsed
      .select("key", "event_time", "country", "temperature", "humidity")
      .writeStream
      .format("console")
      .outputMode("append")
      .option("truncate", "false")
      .option("checkpointLocation", CHECKPOINT_DIR)
      .start()

    query.awaitTermination()
  }
}
