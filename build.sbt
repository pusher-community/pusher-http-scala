name := "pusher-http-scala"

version := "1.0.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http" % "0.8.10",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)
