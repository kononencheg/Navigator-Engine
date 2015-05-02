organization := "ru.livetex"
name := "navigator-engine"

version := "1.0"

scalaVersion := "2.11.6"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akka = "2.3.9"
  val spray = "1.3.3"
  val lucene = "5.1.0"
  val jsoup = "1.8.2"

  Seq(
    "io.spray"          %%  "spray-can"               % spray,
    "com.typesafe.akka" %%  "akka-actor"              % akka,
    "org.apache.lucene" %   "lucene-core"             % lucene,
    "org.apache.lucene" %   "lucene-analyzers-common" % lucene,
    "org.apache.lucene" %   "lucene-queryparser"      % lucene,
    "org.apache.lucene" %   "lucene-queries"          % lucene,
    "org.jsoup"         %   "jsoup"                   % jsoup
  )
}
