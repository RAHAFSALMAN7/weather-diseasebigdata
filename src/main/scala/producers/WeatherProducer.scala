import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._

object WeatherProducer {

  implicit val formats: DefaultFormats.type = DefaultFormats

  val apiKey = "36adc750b2802924fc29c21f811dbfd7"

  val locations = Seq(
    ("Jordan", 31.95, 35.91),
    ("Palestine", 31.90, 35.20),
    ("Turkey", 39.93, 32.85),
    ("Brazil", -15.79, -47.88),
    ("UAE", 25.20, 55.27)
  )

  def getWeather(lat: Double, lon: Double): (Double, Double) = {
    val url =
      s"https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"

    val response = Source.fromURL(url).mkString
    val json = parse(response)

    val temp = (json \ "main" \ "temp").extract[Double]
    val humidity = (json \ "main" \ "humidity").extract[Double]

    (temp, humidity)
  }

  def main(args: Array[String]): Unit = {

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    println("🌍 Fetching LIVE Weather from OpenWeather API...")

    while (true) {
      for ((country, lat, lon) <- locations) {

        val (temp, humidity) = getWeather(lat, lon)

        val json =
          s"""
             {
               "country": "$country",
               "temperature": $temp,
               "humidity": $humidity
             }
           """.stripMargin

        val record = new ProducerRecord[String, String]("weather_topic", country, json)
        producer.send(record)

        println(s"✔ Sent -> $json")
        Thread.sleep(2000)
      }
    }
  }
}
