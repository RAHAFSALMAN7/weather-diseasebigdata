libraryDependencies ++= Seq(
  // Spark
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql"  % "3.5.0",

  // Kafka for Structured Streaming
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.0",
  "org.apache.spark" %% "spark-token-provider-kafka-0-10" % "3.5.0",
  "org.apache.kafka" %  "kafka-clients" % "3.6.0",

  // MongoDB Spark Connector
  "org.mongodb.spark" %% "mongo-spark-connector" % "10.3.0",

  // JSON
  "org.json4s" %% "json4s-native"  % "3.7.0-M11",
  "org.json4s" %% "json4s-jackson" % "3.7.0-M11",
  "org.json4s" %% "json4s-ast"     % "3.7.0-M11"
)

Compile / run / fork := true
