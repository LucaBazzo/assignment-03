name := "assignment-03-actors"

version := "0.1"

scalaVersion := "2.13.8"

lazy val akkaVersion = "2.6.14"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-multi-node-testkit"    % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,

  "io.vertx" % "vertx-core" % "4.0.2",
  "io.vertx" % "vertx-web" % "4.2.6",
  "io.vertx" % "vertx-web-client" % "4.2.6",

  "org.apache.pdfbox" % "pdfbox" % "2.0.22",
)