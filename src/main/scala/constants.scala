package github.interaction.docsearcher.constants

import java.nio.file.Paths

object DEFAULT_CONFIGS {
  val INDEX_PATH = Paths.get(System.getProperty("user.home"), ".lucene_indexs")
  val DOC_PATH = "/Users/interaction/workspace/temp/testeddocs"
  val INGORE_FILE_SET = Set(".DS_Store")
}


object DOC_FIELDS {
  val PATH = "PATH"
  val CONTENT = "CONTENT"
  val LAST_MODIFIED = "LAST_MODIFIED"
}