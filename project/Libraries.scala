import sbt._

object Libraries {

  def onCompile(dep: ModuleID): ModuleID = dep % "compile"
  def onTest(dep: ModuleID): ModuleID = dep % "test"


  object lucene {
    def lucene(module: String) =  "org.apache.lucene" % s"lucene-$module" % "5.5.0"
    lazy val core = lucene("core")
    lazy val queryParser = lucene("queryparser")
    lazy val analyzer = lucene("analyzers-common")

  }
}