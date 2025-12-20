package producers

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.{Properties, Timer, TimerTask}
import scala.io.Source
import scala.util.Random
import java.time.LocalDate
import java.time.ZoneOffset
/**
 * Influenza Producer
 * ----------------------------------
 * This Kafka producer simulates real-time influenza disease data.
 * It reads records from a CSV file, converts them into JSON format,
 * and sends them periodically to a Kafka topic.
 * This producer is used as a data source for Spark Structured Streaming.
 */
object DiseaseProducer {
  // Path to influenza CSV file
  val influenzaFile =
    "data/raw/diseases/influenza/influenza.csv"

  // Read CSV file and skip the header row
  // All data rows are loaded into memory for random sampling
  val influenzaData =
    Source.fromFile(influenzaFile).getLines().drop(1).toList

  // Random generator to simulate different events over time
  val rnd = new Random()

  /**
   * Sends one influenza event to Kafka.
   * A random row is selected from the CSV file to simulate streaming data.
   */
  def sendInfluenzaEvent(producer: KafkaProducer[String, String]): Unit = {

    // If the CSV file is empty, do not send any events
    if (influenzaData.isEmpty) return

    val row = influenzaData(rnd.nextInt(influenzaData.length))
    val cols = row.split(",")

    // Ensure the row has the expected number of columns
    if (cols.length < 4) return


    // Columns from CSV

    // Column 0: country name
    val country = cols(0).trim
    // Column 2: date of the record
    val dateStr = cols(2).trim

    // Column 3: infection rate
    val infectionRate =
      // try-catch is used to avoid runtime errors from invalid values
      try cols(3).toDouble
      catch { case _: Throwable => 0.0 }


    // Convert date -> event_time (ISO Timestamp)
    // Using a unified timestamp format simplifies later data integration
    val eventTime =
      try {
        LocalDate
          .parse(dateStr)
          .atStartOfDay()
          .toInstant(ZoneOffset.UTC)
          .toString
      } catch {
        case _: Throwable => return
      }


    // Create JSON message to send to Kafka
    val json =
      s"""
         {
           "disease": "influenza",
           "country": "$country",
           "infection_rate": $infectionRate,
           "event_time": "$eventTime"
         }
       """.stripMargin

    // Create Kafka record
    // The country is used as the key
    val record =
      new ProducerRecord[String, String](
        "disease_topic",
        country,
        json
      )
    // Send the record to Kafka
    producer.send(record)

    // Console output for monitoring and debugging
    println(s"🦠 Sent Influenza Event -> $country | rate=$infectionRate | time=$eventTime")
  }

  def main(args: Array[String]): Unit = {

    // Kafka Producer configuration
    println("🦠 Fetching LIVE Influenza data from CSV and streaming to Kafka...")
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )

    // Create Kafka producer instance
    val producer = new KafkaProducer[String, String](props)


    // Send event every 2 seconds
    // This simulates continuous real-time data streaming
    val timer = new Timer()
    timer.schedule(
      new TimerTask {
        override def run(): Unit =
          sendInfluenzaEvent(producer)
      },
      0,
      2000
    )
  }
}
