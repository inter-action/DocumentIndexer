package com.github.interaction.docsearcher

import java.io.{BufferedReader, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, SimpleFileVisitor}
import java.util.Date
import java.util.function.Consumer

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.store.FSDirectory


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

object IndexSearcher {
  //todo:
  var indexPath = ""
  var docsPath = ""
  var create = true

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
    doc.add(new LongPoint("modified", lastModified))
    doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))))

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
      Files.walk(path, 100).forEach(new Consumer[Path] {
        override def accept(file: Path): Unit = {

          indexDocs(writer, file, Files.getLastModifiedTime(file).toMillis)
        }
      })
    } else {
      indexDocs(writer, path, Files.getLastModifiedTime(path).toMillis)
    }
  }


  def main(args: Array[String]): Unit = {

    val usage =
      s"""
         | [-index INDEX_PATH] [-docs DOCS_PATH] [-update]
         | This indexes the documents in DOCS_PATH, creating a lucene index
         | in INDEX_PATH that can be searched with SearchFiles
       """.stripMargin


    val argsMap = ArgumentParser.parse(args.mkString(" "))
    if (argsMap.get("index").isDefined) {
      indexPath = argsMap.get("index").get
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
      val dir = FSDirectory.open(Paths.get(indexPath))
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
      case e =>
        println(s"fatal error, ${e.getMessage}")
        System.exit(1)
    }


  }


}