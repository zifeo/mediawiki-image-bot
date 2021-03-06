name := "mediawiki-image-bot"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint:_"
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.4",
  "net.sourceforge" % "jwbf" % "3.1.0",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0",
  "com.flickr4java" % "flickr4java" % "2.16" exclude("log4j", "log4j"),
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21",
  "org.scalacheck" %% "scalacheck" % "1.13.0" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

assemblyJarName in assembly := "mediawiki-image-bot.jar"
test in assembly := {}
mainClass in assembly := Some("bot.scenarios.Main")