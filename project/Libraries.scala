import sbt._

object Libraries {

  def onCompile(dep: ModuleID): ModuleID = dep % "compile"
  def onTest(dep: ModuleID): ModuleID = dep % "test"


  object lucene {
    def lucene(module: String) =  "org.apache.lucene" % s"lucene-$module" % "5.5.0"
    lazy val core = lucene("core")
    lazy val queryParser = lucene("queryparser")
    lazy val analyzersCommon = lucene("analyzers-common")
    lazy val highlighter = lucene("highlighter")
  }

  object akka {
    // jdk 7, since 2.4.0 akka requires jdk 8
    def akka(module: String) = "com.typesafe.akka" %% s"akka-${module}" % "2.4.10"

    lazy val http = akka("http-experimental")
    lazy val sprayJson = akka("http-spray-json-experimental")
    // new version, 2.3.15
    lazy val slf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.3.9"
  }
   
}