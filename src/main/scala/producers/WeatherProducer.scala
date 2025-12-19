import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._
import java.time.Instant

object WeatherProducer {

  implicit val formats: DefaultFormats.type = DefaultFormats

  val apiKey = "36adc750b2802924fc29c21f811dbfd7"

  val locations = Seq(
    ("Afghanistan", 34.52, 69.18),
    ("Albania", 41.33, 19.82),
    ("Algeria", 36.75, 3.04),
    ("Argentina", -34.60, -58.38),
    ("Armenia", 40.18, 44.51),
    ("Australia", -35.28, 149.13),
    ("Austria", 48.20, 16.37),
    ("Azerbaijan", 40.41, 49.86),
    ("Bangladesh", 23.81, 90.41),
    ("Belgium", 50.85, 4.35),
    ("Brazil", -15.79, -47.88),
    ("Canada", 45.42, -75.69),
    ("China", 39.90, 116.40),
    ("Egypt", 30.04, 31.23),
    ("France", 48.85, 2.35),
    ("Germany", 52.52, 13.40),
    ("India", 28.61, 77.20),
    ("Indonesia", -6.21, 106.85),
    ("Iran", 35.68, 51.41),
    ("Iraq", 33.31, 44.36),
    ("Ireland", 53.34, -6.26),
    ("Israel", 31.77, 35.21),
    ("Italy", 41.90, 12.49),
    ("Japan", 35.68, 139.69),
    ("Jordan", 31.95, 35.91),
    ("Kenya", -1.29, 36.82),
    ("Kuwait", 29.37, 47.98),
    ("Lebanon", 33.89, 35.50),
    ("Malaysia", 3.14, 101.69),
    ("Mexico", 19.43, -99.13),
    ("Morocco", 34.02, -6.83),
    ("Netherlands", 52.37, 4.90),
    ("Nigeria", 9.08, 7.40),
    ("Norway", 59.91, 10.75),
    ("Pakistan", 33.69, 73.06),
    ("Philippines", 14.59, 120.98),
    ("Poland", 52.23, 21.01),
    ("Portugal", 38.72, -9.13),
    ("Qatar", 25.29, 51.53),
    ("Saudi Arabia", 24.71, 46.67),
    ("South Africa", -25.74, 28.19),
    ("South Korea", 37.56, 126.97),
    ("Spain", 40.42, -3.70),
    ("Sweden", 59.33, 18.07),
    ("Switzerland", 46.95, 7.44),
    ("Turkey", 39.93, 32.85),
    ("United Arab Emirates", 24.45, 54.38),
    ("United Kingdom", 51.50, -0.12),
    ("United States", 38.90, -77.03),
    ("Vietnam", 21.03, 105.85)
  )

  def getWeather(lat: Double, lon: Double): Map[String, Any] = {

    val url =
      s"https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"

    val source = Source.fromURL(url, "UTF-8")
    try {
      val json = parse(source.mkString)

      Map(
        "temperature" -> (json \ "main" \ "temp").extract[Double],
        "feels_like"  -> (json \ "main" \ "feels_like").extract[Double],
        "humidity"    -> (json \ "main" \ "humidity").extract[Int],
        "pressure"    -> (json \ "main" \ "pressure").extract[Int],
        "wind_speed"  -> (json \ "wind" \ "speed").extract[Double],
        "wind_deg"    -> (json \ "wind" \ "deg").extractOpt[Int].getOrElse(0),
        "cloudiness"  -> (json \ "clouds" \ "all").extract[Int],
        "rain_1h"     -> (json \ "rain" \ "1h").extractOpt[Double].getOrElse(0.0),
        "snow_1h"     -> (json \ "snow" \ "1h").extractOpt[Double].getOrElse(0.0)
      )

    } finally {
      source.close()
    }
  }

  def main(args: Array[String]): Unit = {

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    println("🌍 Fetching LIVE Weather from OpenWeather API (extended fields)...")

    while (true) {
      for ((country, lat, lon) <- locations) {

        val w = getWeather(lat, lon)
        val eventTime = Instant.now().toString


        val jsonStr =
          s"""
             {
               "country": "$country",
               "temperature": ${w("temperature")},
               "feels_like": ${w("feels_like")},
               "humidity": ${w("humidity")},
               "pressure": ${w("pressure")},
               "wind_speed": ${w("wind_speed")},
               "wind_deg": ${w("wind_deg")},
               "cloudiness": ${w("cloudiness")},
               "rain_1h": ${w("rain_1h")},
               "snow_1h": ${w("snow_1h")},
               "event_time": "$eventTime"
             }
           """.stripMargin

        val record = new ProducerRecord[String, String](
          "weather_topic",
          country,
          jsonStr
        )

        producer.send(record)
        println(s"✔ Sent -> $jsonStr")

        Thread.sleep(2000)
      }
    }
  }
}
