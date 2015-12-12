name := "pusher-http-scala"

version := "0.0.1"

scalaVersion := "2.11.6"

val specsVer = "3.6.4"

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
  "org.specs2"    %%  "specs2-core"   % specsVer % "test",
  "org.specs2"    %%  "specs2-matcher" % specsVer % "test",
  "org.specs2"    %%  "specs2-matcher-extra" % specsVer % "test",
  "org.specs2"    %%  "specs2-mock"   % specsVer % "test"
)

(unmanagedSourceDirectories in Compile) <+= baseDirectory(_/"example")