name := "Event Sourcing Demo"

version := "0.0.1-SNAPSHOT"

organization := "com.appdirect"

scalaVersion := "2.11.7"

parallelExecution in Test := false

libraryDependencies ++= {
  val logbackVersion = "1.1.2"
  val akkaStreamV = "1.0-RC4"
  val akkaVersion = "2.3.11"
  Seq(
    "org.slf4j" % "slf4j-api" % "1.7.7",
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
    "com.github.scullxbones" %% "akka-persistence-mongo-rxmongo" % "0.4.0",
    "org.reactivemongo" %% "reactivemongo" % "0.11.1",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
  )
}

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
