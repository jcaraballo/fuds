name := "fuds"

version := Option(System.getenv("FUDS_BUILD_NUMBER")).getOrElse("dev-local")

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "Tim Tennant's repo" at "http://dl.bintray.com/timt/repo/",
  Resolver.sonatypeRepo("public")
)

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-directives" % "0.8.0",
  "net.databinder" %% "unfiltered-filter" % "0.8.0",
  "net.databinder" %% "unfiltered-filter-async" % "0.8.0",
  "net.databinder" %% "unfiltered-jetty" % "0.8.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
  "io.shaka" %% "naive-http" % "51" % "test",
  "com.github.scopt" %% "scopt" % "3.2.0"
)
