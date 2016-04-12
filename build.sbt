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
  "ch.qos.logback" % "logback-classic" % "1.1.6",
  "net.sourceforge" % "jwbf" % "3.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.2" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.0" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
