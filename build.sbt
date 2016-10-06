import Libraries._

name := "document-indexer"

organization := "com.github.interaction"

version := "0.1.0"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8")

resolvers ++= Seq(
  // can i remove this following two?
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/", //akka
  Resolver.bintrayRepo("hseeberger", "maven") // json4s
)

libraryDependencies ++= {
  val Json4sVersion = "3.2.11"

  Seq(
    lucene.core,
    lucene.queryParser,
    lucene.analyzersCommon,
    lucene.highlighter,
    // start: akka http rest
    akka.http,
    akka.slf4j,
    akka.sprayJson,
    "org.json4s"        %% "json4s-native"   % Json4sVersion,
    "org.json4s"        %% "json4s-ext"      % Json4sVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.1.2",
    "de.heikoseeberger" %% "akka-http-json4s" % "1.4.2",
    // end: akka http rest
    "org.scalactic" %% "scalactic" % "3.0.0",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  )
}

// http://www.scala-lang.org/files/archive/nightly/docs-2.10.2/manual/html/scalac.html
// http://blog.threatstack.com/useful-scalac-options-for-better-scala-development-part-1
scalacOptions ++= Seq(
    "-target:jvm-1.8", // enforce jdk
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-language:postfixOps",
    "-unchecked",
    //lucene searcher highlight component require this deprecated api :(
//    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint",// Enable recommended additional warnings.
    "-Yinline-warnings",
    "-Ywarn-dead-code",
    "-Xfuture",
    "-Yno-adapted-args"
)

initialCommands := "com.github.interaction.docsearcher._"