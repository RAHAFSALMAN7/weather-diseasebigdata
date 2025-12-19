import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object WeatherStream {

  val KAFKA_BOOTSTRAP = "localhost:9092"
  val TOPIC = "weather_topic"
  val CHECKPOINT_DIR =
    "file:///D:/BD_Project/weather-diseasebigdata/checkpoint/weatherStream"

  val weatherSchema: StructType = new StructType()
    .add("country", StringType, nullable = true)
    .add("temperature", DoubleType, nullable = true)
    .add("feels_like", DoubleType, nullable = true)
    .add("humidity", IntegerType, nullable = true)
    .add("pressure", IntegerType, nullable = true)
    .add("wind_speed", DoubleType, nullable = true)
    .add("wind_deg", IntegerType, nullable = true)
    .add("cloudiness", IntegerType, nullable = true)
    .add("rain_1h", DoubleType, nullable = true)
    .add("snow_1h", DoubleType, nullable = true)
    .add("event_time", StringType, nullable = true)

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
        col("json.feels_like").as("feels_like"),
        col("json.humidity").as("humidity"),
        col("json.pressure").as("pressure"),
        col("json.wind_speed").as("wind_speed"),
        col("json.wind_deg").as("wind_deg"),
        col("json.cloudiness").as("cloudiness"),
        col("json.rain_1h").as("rain_1h"),
        col("json.snow_1h").as("snow_1h"),
        col("json.event_time").as("event_time")
      )

    val query = parsed
      .select(
        "key",
        "event_time",
        "country",
        "temperature",
        "feels_like",
        "humidity",
        "pressure",
        "wind_speed",
        "wind_deg",
        "cloudiness",
        "rain_1h",
        "snow_1h"
      )
      .writeStream
      .format("console")
      .outputMode("append")
      .option("truncate", "false")
      .option("checkpointLocation", CHECKPOINT_DIR)
      .start()

    query.awaitTermination()
  }
}
