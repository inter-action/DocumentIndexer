import Libraries._

name := "document-indexer"

organization := "com.github.interaction"

version := "0.1.0"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8")

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Artima Maven Repository" at "http://repo.artima.com/releases" 
)


libraryDependencies ++= Seq(
  lucene.core,
  lucene.queryParser,
  lucene.analyzer,
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

// http://www.scala-lang.org/files/archive/nightly/docs-2.10.2/manual/html/scalac.html
// http://blog.threatstack.com/useful-scalac-options-for-better-scala-development-part-1
scalacOptions ++= Seq(
    "-target:jvm-1.7", // enforce jdk
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",// Enable recommended additional warnings.
    "-Yinline-warnings",
    "-Ywarn-dead-code",
    "-Xfuture",
    "-Yno-adapted-args"
)

initialCommands := "com.github.interaction.docsearcher._"