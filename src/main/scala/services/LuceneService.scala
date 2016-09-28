package github.interaction.docsearcher.services

import java.io.{BufferedReader, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.Date

import github.interaction.docsearcher.constants.DEFAULT_CONFIGS
import github.interaction.docsearcher.entities._
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.store.FSDirectory

import scala.concurrent.{ExecutionContext, Future}

class LuceneService(implicit val executionContext: ExecutionContext) {

  //todo: replace Either with scalaz one, replace println with logger
  def createIndex(indexUpdaterModel: IndexUpdaterModel): Future[Option[Throwable]] = Future {
    val indexPath =
      indexUpdaterModel.indexPath
        .map(Paths.get(_))
        .getOrElse(DEFAULT_CONFIGS.INDEX_PATH)
    val isUpdate = indexUpdaterModel.isUpdate.getOrElse(false)

    val docDir = Paths.get(indexUpdaterModel.docPath)
    if (!Files.isReadable(docDir)) {
      val throwable = new RuntimeException(s"Document directory ${docDir.toAbsolutePath} doesnt exist or is not readable")
      //todo: why have to wrap around with Future ? does return type turn into Future[Future[Option[T]]]?
      return Future(Some(throwable))
    }

    val start = new Date()
    try {
      val dir = FSDirectory.open(indexPath)
      val analyzer = new StandardAnalyzer()
      val iwc = new IndexWriterConfig(analyzer)

      if (isUpdate) {
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
      None

    } catch {
      case e: Throwable =>
        println(s"fatal error, ${e.getMessage}")
        Some(e)
    }


  }


  private def indexDocs(writer: IndexWriter, file: Path, lastModified: Long): Unit = {

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

