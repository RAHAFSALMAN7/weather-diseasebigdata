package producers

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.{Properties, Timer, TimerTask}
import scala.io.Source
import scala.util.Random

object DiseaseProducer {

  // ============================
  // 1) دوال تنظيف آمنة  (Scala 2)
  // ============================

  def cleanCell(cell: String): String = {
    if (cell == null) return "0"

    val trimmed = cell.trim

    if (trimmed.contains(";")) {
      // نأخذ أول قيمة رقمية فقط
      val parts = trimmed.split(";")
      val firstNumber = parts.find(p => p.matches("[-]?[0-9.]+"))
      firstNumber.getOrElse("0")
    } else {
      trimmed
    }
  }

  def safeDouble(value: String): Double = {
    try cleanCell(value).toDouble
    catch { case _: Throwable => 0.0 }
  }

  def safeInt(value: String): Int = {
    try cleanCell(value).toInt
    catch { case _: Throwable => 0 }
  }

  // ============================================
  // 2) تحميل ملفات CSV
  // ============================================

  val allergyFile = "data/raw/diseases/allergy/AirQuality.csv"
  val heatStrokeFile = "data/raw/diseases/heat_stroke/Heat_Stroke.csv"
  val influenzaFile = "data/raw/diseases/influenza/influenza_weekly.csv"

  val allergyData = Source.fromFile(allergyFile).getLines().drop(1).toList
  val heatStrokeData = Source.fromFile(heatStrokeFile).getLines().drop(1).toList
  val influenzaData = Source.fromFile(influenzaFile).getLines().drop(1).toList

  val rnd = new Random()

  // ============================================
  // 3) إرسال حدث مرض عشوائي
  // ============================================

  def sendDiseaseEvent(producer: KafkaProducer[String, String]): Unit = {

    val picker = rnd.nextInt(3)
    val (diseaseName, fileRows) =
      if (picker == 0) ("allergy", allergyData)
      else if (picker == 1) ("heat_stroke", heatStrokeData)
      else ("influenza", influenzaData)

    if (fileRows.isEmpty) return

    val row = fileRows(rnd.nextInt(fileRows.length))

    // نقسم السطر حسب فاصلة أو فاصلة منقوطة
    val columns = row.split("[,;]").map(cleanCell)

    val feature1 = safeDouble(columns.headOption.getOrElse("0"))
    val feature2 = safeDouble(if (columns.length > 1) columns(1) else "0")
    val feature3 = safeDouble(if (columns.length > 2) columns(2) else "0")

    val json =
      s"""
         {
           "disease": "$diseaseName",
           "feature1": $feature1,
           "feature2": $feature2,
           "feature3": $feature3,
           "timestamp": ${System.currentTimeMillis()}
         }
       """.stripMargin

    val rec = new ProducerRecord[String, String]("disease_topic", diseaseName, json)
    producer.send(rec)

    println("✔ Sent disease event: " + json)
  }

  // ============================================
  // 4) Main – التشغيل
  // ============================================

  def main(args: Array[String]): Unit = {

    println("🚑 DiseaseProducer started...")

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val timer = new Timer()
    timer.schedule(
      new TimerTask {
        override def run(): Unit = sendDiseaseEvent(producer)
      },
      0,
      2000 // كل ثانيتين
    )
  }
}
