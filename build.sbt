name := "event souricng example"

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
  val sprayVersion = "1.3.2"
  val akkaVersion = "2.3.11"
  Seq(
    "org.slf4j"               %   "slf4j-api"       % "1.7.7",
    "ch.qos.logback"          %   "logback-core"    % logbackVersion,
    "ch.qos.logback"          %   "logback-classic" % logbackVersion,
    "io.spray"                %%  "spray-can"       % sprayVersion,
    "io.spray"                %%  "spray-routing"   % sprayVersion,
    "io.spray"                %%  "spray-testkit"   % sprayVersion,
    "io.spray"                %%  "spray-httpx"     % sprayVersion,
    "io.spray"                %%  "spray-client"    % sprayVersion,
    "org.json4s"              %%  "json4s-native"   % "3.2.10",
    "joda-time"               %   "joda-time"       % "2.4",
    "org.joda"                %   "joda-convert"    % "1.7",
    "com.typesafe.akka"       %%  "akka-actor"      % akkaVersion,
    "com.typesafe.akka"       %%  "akka-slf4j"      % akkaVersion,
    "com.typesafe.akka"       %%  "akka-testkit"    % akkaVersion % "test",
    "com.typesafe.akka"       %%  "akka-persistence-experimental" % akkaVersion,
    "org.scalatest"           %%  "scalatest"       % "2.2.1" % "test",
    "io.spray"                %%  "spray-testkit"   % "1.3.1" % "test"
  )
}
