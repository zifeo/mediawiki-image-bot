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
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21",
  "net.sourceforge" % "jwbf" % "3.1.0",
  "com.flickr4java" % "flickr4java" % "2.16",
  "org.scalacheck" %% "scalacheck" % "1.13.0" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
