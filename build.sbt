ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "meeting-room-ai-scala",
    libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.3.3"
    , "org.scalatest" %% "scalatest" % "3.2.7" % Test
    , "org.postgresql" % "postgresql" % "42.3.1"
    , "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
    , "com.typesafe.akka" %% "akka-http" % "10.2.4"
    , "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.4"
    , "com.typesafe.akka" %% "akka-stream" % "2.6.14"
  )
  )
