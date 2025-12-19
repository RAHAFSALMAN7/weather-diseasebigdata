package streaming

import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.util.sketch.BloomFilter
import org.apache.spark.storage.StorageLevel

object WeatherDiseaseJoinStream {

  // =========================
   // =========================
  val KAFKA_BOOTSTRAP = "localhost:9092"
  val WEATHER_TOPIC   = "weather_topic"
  val DISEASE_TOPIC   = "disease_topic"

  val MONGO_URI  = "mongodb://localhost:27017"
  val MONGO_DB   = "weather_disease_db"
  val MONGO_COLL = "seasonal_analysis"

  // =========================
   // =========================
  val weatherSchema: StructType = new StructType()
    .add("country", StringType)
    .add("temperature", DoubleType)
    .add("feels_like", DoubleType)
    .add("humidity", IntegerType)
    .add("pressure", IntegerType)
    .add("wind_speed", DoubleType)
    .add("cloudiness", IntegerType)
    .add("event_time", StringType)

  val diseaseSchema: StructType = new StructType()
    .add("disease", StringType)
    .add("country", StringType)
    .add("infection_rate", DoubleType)
    .add("event_time", StringType)

  // =========================
   // =========================
  def normalizeCountry(c: Column): Column =
    lower(trim(c))

  def parseTs(c: Column): Column =
    coalesce(
      to_timestamp(c, "yyyy-MM-dd'T'HH:mm:ssX"),
      to_timestamp(c, "yyyy-MM-dd HH:mm:ss"),
      to_timestamp(c)
    )

  def seasonFromMonth(m: Column): Column =
    when(m.isin(12, 1, 2), "Winter")
      .when(m.isin(3, 4, 5), "Spring")
      .when(m.isin(6, 7, 8), "Summer")
      .otherwise("Autumn")

  // =========================
   // =========================
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("WeatherDiseaseJoinStream")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "8")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // =====================================================
     // =====================================================
    val diseaseStatic = spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP)
      .option("subscribe", DISEASE_TOPIC)
      .option("startingOffsets", "earliest")
      .option("endingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) AS value")
      .filter(length(trim(col("value"))) > 0)
      .withColumn("json", from_json(col("value"), diseaseSchema))
      .filter(col("json").isNotNull)
      .select(
        normalizeCountry(col("json.country")).as("country"),
        col("json.disease").as("disease"),
        col("json.infection_rate").as("infection_rate"),
        parseTs(col("json.event_time")).as("event_time")
      )
      .filter(col("country").isNotNull && length(col("country")) > 0 && col("event_time").isNotNull)
      .withColumn("month", month(col("event_time")))
      .withColumn("season", seasonFromMonth(col("month")))
      .drop("month")

    diseaseStatic.persist(StorageLevel.MEMORY_AND_DISK)

    // =====================================================
     // =====================================================
    val diseaseCountries: Array[String] =
      diseaseStatic
        .select("country")
        .distinct()
        .collect()
        .map(_.getString(0))

    if (diseaseCountries.isEmpty) {
      System.err.println(":x: No countries found in disease_topic. Bloom filter cannot be built.")
      System.exit(1)
    }

    val bloom = BloomFilter.create(diseaseCountries.length.toLong, 0.01)
    diseaseCountries.foreach(bloom.put)

    val bloomBC = spark.sparkContext.broadcast(bloom)

    // UDF آمن للستريمنج (بديل TypedFilter)
    val bloomContains = udf { c: String =>
      if (c == null) false else bloomBC.value.mightContain(c)
    }

    println(s":white_check_mark: Bloom Filter initialized with ${diseaseCountries.length} countries")

    // =====================================================
     // =====================================================
    val diseaseSeasonal =
      diseaseStatic
        .groupBy("country", "season", "disease")
        .agg(avg("infection_rate").as("avg_infection_rate"))
        .persist(StorageLevel.MEMORY_AND_DISK)

    // =====================================================
     // =====================================================
    val weatherStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP)
      .option("subscribe", WEATHER_TOPIC)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) AS value")
      .filter(length(trim(col("value"))) > 0)
      .withColumn("json", from_json(col("value"), weatherSchema))
      .filter(col("json").isNotNull)
      .select(
        normalizeCountry(col("json.country")).as("country"),
        col("json.temperature").as("temperature"),
        col("json.feels_like").as("feels_like"),
        col("json.humidity").cast(DoubleType).as("humidity"),
        col("json.pressure").cast(DoubleType).as("pressure"),
        col("json.wind_speed").as("wind_speed"),
        col("json.cloudiness").cast(DoubleType).as("cloudiness"),
        parseTs(col("json.event_time")).as("event_time")
      )
      .filter(col("country").isNotNull && col("event_time").isNotNull)
      .withColumn("month", month(col("event_time")))
      .withColumn("season", seasonFromMonth(col("month")))
      .drop("month")

    // =====================================================
     // =====================================================
    val weatherFiltered =
      weatherStream.filter(bloomContains(col("country")))

    // =====================================================
     // =====================================================
    val checkpointPath =
      "C:/weather-diseasebigdata/data/checkpoints/Streaming/diseaseWeatherJoinStream"

    val query = weatherFiltered.writeStream
      .option("checkpointLocation", checkpointPath)
      .foreachBatch { (weatherBatch: DataFrame, batchId: Long) =>

        if (!weatherBatch.isEmpty) {

          val weatherSeasonal =
            weatherBatch
              .groupBy("country", "season")
              .agg(
                avg("temperature").as("avg_temperature"),
                avg("feels_like").as("avg_feels_like"),
                avg("humidity").as("avg_humidity"),
                avg("wind_speed").as("avg_wind_speed"),
                avg("pressure").as("avg_pressure"),
                avg("cloudiness").as("avg_cloudiness")
              )
              .withColumn(
                "humidity_level",
                when(col("avg_humidity") < 40, "Low")
                  .when(col("avg_humidity") < 60, "Medium")
                  .otherwise("High")
              )

          val joined =
            diseaseSeasonal
              .join(weatherSeasonal, Seq("country", "season"), "inner")
              .withColumn(
                "weather_stress_index",
                (when(col("avg_temperature") < 10, 1).otherwise(0) +
                  when(col("avg_humidity") < 40, 1).otherwise(0) +
                  when(col("avg_wind_speed") > 5, 1).otherwise(0)).cast("int")
              )
              .withColumn("ingested_at", current_timestamp())

          joined.write
            .format("mongodb")
            .mode("append")
            .option("connection.uri", MONGO_URI)
            .option("database", MONGO_DB)
            .option("collection", MONGO_COLL)
            .save()

          println(s":white_check_mark: Batch $batchId written to MongoDB")
        } else {
          println(s":information_source: Batch $batchId is empty. Skipping.")
        }
      }
      .start()

    query.awaitTermination()
  }
}
