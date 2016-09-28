package github.interaction.docsearcher.constants

import java.nio.file.Paths

object DEFAULT_CONFIGS {
  val INDEX_PATH = Paths.get(System.getProperty("user.home"), ".lucene_indexs")
  val DOC_PATH = "/Users/interaction/workspace/temp/testeddocs"
  val CONTENT_FIELD = "contents"
  val INGORE_FILE_SET = Set(".DS_Store")
}
