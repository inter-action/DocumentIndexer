package github.interaction.docsearcher

import java.io.{BufferedReader, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.Date

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, Query}
import org.apache.lucene.store.FSDirectory

import scala.io.StdIn



/*
http://lucene.apache.org/core/5_5_0/demo/overview-summary.html#IndexFiles
 */


object ArgumentParser {
  def parse(str: String): Map[String, String] = {

    val ts = str.split("-").map { e =>
      val t = e.split(" ").map(_.trim)
      if (t.length == 2) {
        Some((t(0), t(1)))
      } else if (t.length == 1) {
        Some((t(0), ""))
      } else {
        None
      }
    }

    ts.filter(_.isDefined).map(_.get).toMap[String, String]
  }
}

object DEFAULT_CONFIGS {
  val INDEX_PATH = Paths.get(System.getProperty("user.home"), ".lucene_indexs")
  val DOC_PATH = "/Users/interaction/workspace/temp/testeddocs"
  val CONTENT_FIELD = "contents"
  val INGORE_FILE_SET = Set(".DS_Store")
}



object Indexer {
  var indexPath = DEFAULT_CONFIGS.INDEX_PATH
  var docsPath = DEFAULT_CONFIGS.DOC_PATH
  var create = true

  def main(args: Array[String]): Unit = {

    val usage =
      s"""
         | [-index INDEX_PATH] [-docs DOCS_PATH] [-update]
         | This indexes the documents in DOCS_PATH, creating a lucene index
         | in INDEX_PATH that can be searched with SearchFiles
       """.stripMargin


    val argsMap = ArgumentParser.parse(args.mkString(" "))
    if (argsMap.get("index").isDefined) {
      indexPath = Paths.get(argsMap.get("index").get)
    }
    if (argsMap.get("docs").isDefined) {
      docsPath = argsMap.get("docs").get
    }
    if (argsMap.get("update").isDefined) {
      create = false
    }

    val docDir = Paths.get(docsPath)
    if (!Files.isReadable(docDir)) {
      println(s"Document directory ${docDir.toAbsolutePath} doesnt exist or is not readable")
      System.exit(1)
    }

    val start = new Date()
    try {
      val dir = FSDirectory.open(indexPath)
      val analyzer = new StandardAnalyzer()
      val iwc = new IndexWriterConfig(analyzer)

      if (create) {
        // create a new index in the directory, removing any previously indexed directory
        iwc.setOpenMode(OpenMode.CREATE)
      } else {
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND)
      }
      // limit ram size
      //      iwc.setRAMBufferSizeMB(256.0)

      val writer = new IndexWriter(dir, iwc)
      indexDocs(writer, docDir)

      writer.close()
      val end = new Date()
      println(s"${end.getTime - start.getTime} total miliseconds")


    } catch {
      case e: Throwable =>
        println(s"fatal error, ${e.getMessage}")
        System.exit(1)
    }

  }

  def indexDocs(writer: IndexWriter, file: Path, lastModified: Long): Unit = {

    val stream = Files.newInputStream(file)
    // add the path of the file as a field name "path", use a field that is indexed(ie. searchable)
    // but dont tokenize the filed into separate words and dont index term frequency
    // or position information
    val doc = new Document
    val pathField = new StringField("path", file.toString, Field.Store.YES)
    doc.add(pathField)

    // Add the last modified date of the file a field named "modified".
    // Use a LongPoint that is indexed (i.e. efficiently filterable with
    // PointRangeQuery).  This indexes to milli-second resolution, which
    // is often too fine.  You could instead create a number based on
    // year/month/day/hour/minutes/seconds, down the resolution you require.
    // For example the long value 2011021714 would mean
    // February 17, 2011, 2-3 PM.
    doc.add(new LongField("modified", lastModified, Field.Store.NO))
    doc.add(new TextField(DEFAULT_CONFIGS.CONTENT_FIELD, new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))))

    if (writer.getConfig.getOpenMode == OpenMode.CREATE) {
      println(s"add file: ${file}")
      writer.addDocument(doc)
    } else {
      println(s"updating file: ${file}")
      // new Term("path serve as docId
      writer.updateDocument(new Term("path", file.toString), doc)
    }


  }


  /**
    * Indexes the given file using the given writer, or if a directory is given,
    * recurses over files and directories found under the given directory.
    *
    * NOTE: This method indexes one document per input file.  This is slow.  For good
    * throughput, put multiple documents into your input file(s).  An example of this is
    * in the benchmark module, which can create "line doc" files, one document per line,
    * using the
    * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
    * >WriteLineDocTask</a>.
    *
    * @param writer Writer to the index where the given file/dir info will be stored
    * @param path   The file to index, or the directory to recurse into to find files to index
    */
  def indexDocs(writer: IndexWriter, path: Path): Unit = {

    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path,
                               attrs: BasicFileAttributes): FileVisitResult = {

          indexDocs(writer, file, attrs.lastModifiedTime().toMillis)
          FileVisitResult.CONTINUE

        }
      })

    } else {
      indexDocs(writer, path, Files.getLastModifiedTime(path).toMillis)
    }
  }
}


object Searcher {
  var indexPath = DEFAULT_CONFIGS.INDEX_PATH
  var field = DEFAULT_CONFIGS.CONTENT_FIELD
  var queries = ""
  var repeat = 0
  var queryString = ""
  var hitsPerPage = 10
  var isRaw = false

  /*
  queries: find in file
   */
  def main(args: Array[String]): Unit = {

    val usage =
      """
        |Usage:	 [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]
        |
        |See http://lucene.apache.org/core/4_1_0/demo/ for details.
      """.stripMargin

    val argsMap = ArgumentParser.parse(args.mkString(" "))
    argsMap.foreach {
      case ("index", value) => // index path
        indexPath = Paths.get(value)
      case ("field", value) =>
        field = value
      case ("queries", value) =>
        queries = value
      case ("query", value) =>
        queryString = value
      case ("repeat", value) =>
        repeat = Integer.parseInt(value)
      case ("raw", value) => //
        isRaw = true
      case ("paging", value) => //
        hitsPerPage = Integer.parseInt(value)
        if (hitsPerPage <= 0) {
          System.err.println("There must be at least 1 hit per page")
          System.exit(1)
        }
      case _@e =>
        println(s"we simply ignore empty MapEntry ${e}")
    }

    val reader = DirectoryReader.open(FSDirectory.open(indexPath))
    val searcher = new IndexSearcher(reader)
    val analyzer = new StandardAnalyzer()

    val in = if (queries != "") {//queries文件制定的地方读取第一行作为query string
      Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8)
    } else {
      new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))
    }
    val parser = new QueryParser(field, analyzer)

    var isBreak = false
    while (!isBreak) {
      if (queries == "" && queryString == "") {
        println("Enter query: ")
      }

      val line = if (queryString == "") {
        Option(StdIn.readLine())
      } else {
        Some(queryString)
      }
      val nline = line.filter(_.trim.length != -1)

      if (nline.isEmpty) {
        isBreak = true
      } else {
        val query = parser.parse(nline.get)
        println(s"Searching for: ${query.toString(field)}")
        if (repeat > 0) {
          // repeat & time as benchmark, For benchmark !, dont use it
          val start = new Date()
          for (i <- 0 until repeat) {
            searcher.search(query, 100)
          }
          val end = new Date()
          println(s"Time: ${end.getTime - start.getTime} ms")
        }

        doPagingSearch(in, searcher, query, hitsPerPage, isRaw, queries == "" && queryString == "")

        if (queryString == "") {
          isBreak = true
        }
      }

    }
  }


  def doPagingSearch(in: BufferedReader, searcher: IndexSearcher, query: Query, hitsPerPage: Int, isRaw: Boolean,
                     isInteractive: Boolean) = {

    // Collect enough docs to show 5 pages
    val results = searcher.search(query, 5 * hitsPerPage)
    val hits = results.scoreDocs // this is total doc actually returned from search
    val numTotalHits = results.totalHits // total search doc number that hit
    println(s"${numTotalHits} total matching documents")

    var start = 0
    var end = Math.min(numTotalHits, hitsPerPage)

    var isBreak = false

    def continueIfExceedingCache(): Boolean = {

      if (end > hits.length) {
        // 到达5页的缓存
        // there's more
        println(s"Only results 1 - ${hits.length} of ${numTotalHits} total matching documents collected.")
        // load more doc from searcher
        println("Collect more(y/n) ?")
        val line = in.readLine()
        if (line.length == 0 || line.charAt(0) == 'n') {
          false
        } else {
          true
        }
      } else {
        true
      }
    }

    def askToContinue(): Boolean = {

      var isBreak = false
      var isContinue = false
      while (!isBreak) {
        print("Press")
        if (start - hitsPerPage >= 0) {
          print("(p)revious page,")
        }
        if (start + hitsPerPage < end) {
          print("(n)ext page")
        }
        println("(q)uit or enter number to jump a page.")
        val line = in.readLine()
        if (line.length == 0 || line.charAt(0) == 'q') {
          isBreak = true
        } else {
          line.charAt(0) match {
            case 'q' =>
              isBreak = true
              isContinue = false
            case 'p' =>
              start = Math.max(0, start - hitsPerPage)
              isBreak = true
              isContinue = true
            case 'n' =>
              if (start + hitsPerPage < numTotalHits) {
                start += hitsPerPage
              }
              isBreak = true
              isContinue = true
            case _ =>
              val page = Integer.parseInt(line)
              if ((page - 1) * hitsPerPage < numTotalHits) {
                start = (page - 1) * hitsPerPage
                isBreak = true
                isContinue = true
              } else {
                println("No such page")
              }
          }
        }


      }

      isContinue
    }


    while (!isBreak) {

      if (continueIfExceedingCache()) {
        end = Math.min(hits.length, start + hitsPerPage) // first page

        for (i <- start until end) {
          if (isRaw) {
            println(s"doc=${hits(i).doc} score=${hits(i).score}")
          } else {
            val doc = searcher.doc(hits(i).doc)
            val path = Option(doc.get("path"))
            if (path.isDefined) {
              println(s"${i + 1}. ${path.get}")
              val title = Option(doc.get("title"))
              title.foreach(e => println(s"Title: ${e}"))
            } else {
              println(s"${i + 1}. No path for this document")
            }
          }
        }

        if (!isInteractive || end == 0) {
          isBreak = true
        } else {
          if (numTotalHits >= end) {
            if (!askToContinue()) {
              isBreak = true
            } else {
              end = Math.min(numTotalHits, start + hitsPerPage)
            }
          }
        }
      }
    }
  }
}