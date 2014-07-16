name := "fuds"

version := "1.0"

scalaVersion := "2.11.1"

resolvers += "Tim Tennant's repo" at "http://dl.bintray.com/timt/repo/"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-directives" % "0.8.0",
  "net.databinder" %% "unfiltered-filter" % "0.8.0",
  "net.databinder" %% "unfiltered-filter-async" % "0.8.0",
  "net.databinder" %% "unfiltered-jetty" % "0.8.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"
)
