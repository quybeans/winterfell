// Copyright (C) 2016-2017 Ark Maxim, Inc.

name := "winterfell"

version := "0.1"

scalaVersion := "2.12.3"

val circeVersion = "0.8.0"

// scalastyle:off multiple.string.literals
libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.1.1",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)
// scalastyle:on multiple.string.literals