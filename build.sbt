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

  // JSON library (match Spark 3.5.0)
  "org.json4s" %% "json4s-native"  % "3.7.0-M11",
  "org.json4s" %% "json4s-jackson" % "3.7.0-M11",
  "org.json4s" %% "json4s-ast"     % "3.7.0-M11"

)

Compile / run / fork := true

