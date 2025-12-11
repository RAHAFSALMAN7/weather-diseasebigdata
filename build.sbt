name := "weather-disease-bigdata"

version := "1.0"

scalaVersion := "2.12.18"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  "org.apache.spark" %% "spark-streaming" % "3.5.0",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.0",
  "org.apache.kafka" % "kafka-clients" % "3.6.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.10.0",

  // JSON library
  "org.json4s" %% "json4s-native" % "4.0.6",
  "org.json4s" %% "json4s-jackson" % "4.0.6"
)

Compile / run / fork := true
