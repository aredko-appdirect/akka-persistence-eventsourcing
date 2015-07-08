name := "Event Sourcing Demo"

version := "0.0.1-SNAPSHOT"

organization := "com.appdirect"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "snapshots"           at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"            at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "spray repo"          at "http://repo.spray.io"
)

seq(Revolver.settings: _*)

parallelExecution in Test := false

libraryDependencies ++= {
  val logbackVersion = "1.1.2"
  val akkaStreamV = "1.0-RC4"
  val akkaVersion = "2.3.11"
  Seq(
    "org.slf4j" %   "slf4j-api"       % "1.7.7",
    "ch.qos.logback" %   "logback-core"    % logbackVersion,
    "ch.qos.logback" %   "logback-classic" % logbackVersion,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV,
    "com.typesafe.akka" %%  "akka-actor" % akkaVersion,
    "com.typesafe.akka" %%  "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %%  "akka-testkit" % akkaVersion % "test",
    "com.typesafe.akka" %%  "akka-persistence-experimental" % akkaVersion,
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
  )
}
