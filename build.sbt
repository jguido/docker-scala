organization := "com.unrlab"

name := "docker-scala"

version := "0.0.1"

scalaVersion := "2.11.8"

resolvers += DefaultMavenRepository

val deps = Seq(
  "com.typesafe" % "config" % "1.3.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.5.1",
  "org.scalaj" %% "scalaj-http" % "2.3.0"
)

val testDeps = Seq(
  "org.scalatest"     %% "scalatest" % "3.0.1" % "test",
  "com.jayway.restassured" % "rest-assured" % "2.9.0" % "test"
)

libraryDependencies ++= deps ++ testDeps