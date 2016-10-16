package github.interaction.docsearcher.others

import github.interaction.docsearcher.constants.{DEFAULT_CONFIGS, DOC_FIELDS}
import github.interaction.docsearcher.utils.ResouceUtil
import iweb.ch04.models.{Attribute, NumericDataPoint}
import org.apache.lucene.index.{DirectoryReader, Terms}
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import iweb.ch04.similarity._
import iweb.ch04.similarity.hierarchical.AverageLinkAlgorithm
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

object DocCluster {
  private val logger = LoggerFactory.getLogger(this.getClass)


  private def collectTerms(terms: Terms): Array[String] = {
    val result = ArrayBuffer.empty[String]
    val iterator = terms.iterator()

    var br = iterator.next()
    while (br != null){
      result += br.utf8ToString()
      br = iterator.next()
    }

    result.toArray
  }

  def avergeLinkAlgorithm(): Unit ={
    val metas = docMetaDatas()
    metas match {
      case Left(e) =>
        logger.error("failed to read document index,", e)
      case Right((terms, dps)) =>
        val matrix = simMatrix(terms)
        val ca = new AverageLinkAlgorithm(dps.toArray, matrix)
        val dnd = ca.cluster()
        dnd.printAll()
    }
  }

  //todo: 根据这个函数输出的结果看, 分词工具还是不够好用, 导致最后cluster的文档也有些莫名其妙
  // 等着先把分词工具解决了看看， 或者将一个单词和数字的都去掉
  private def testParsingTerms(): Unit ={
    val metas = docMetaDatas()
    metas match {
      case Left(e) =>
        logger.error("failed to read document index,", e)
      case Right((terms, dps)) =>
        for (i <- 0 until terms.length){
          println(s"${dps(i).label} - ${collectTerms(terms(i)).mkString(",")}")
        }
    }
  }

  def simMatrix(terms: IndexedSeq[Terms]): Array[Array[Double]] ={

    val length = terms.length
    val maxtrix = Array.fill(length, length)(0d)

    for (i <- 0 until length){
      for (j <- i until length){
        val termsI = collectTerms(terms(i))
        val termsJ = collectTerms(terms(j))
        val similarity = CosineSimilarity.similarity(termsI, termsJ)
        maxtrix(i)(j) = similarity
        maxtrix(j)(i) = similarity
      }
    }
    maxtrix


  }


  def docMetaDatas(): Either[Throwable, (IndexedSeq[Terms], IndexedSeq[NumericDataPoint])] ={
    val reader = DirectoryReader.open(FSDirectory.open(DEFAULT_CONFIGS.INDEX_PATH))
    val searcher = new IndexSearcher(reader)
    val indexReader = searcher.getIndexReader

    ResouceUtil.withResoure(indexReader){ r =>
      val result = ListBuffer.empty[Terms]
      val dps = ListBuffer.empty[NumericDataPoint]

      val maxdocs = reader.maxDoc()
      for (i <- 0 until maxdocs){
        val doc = reader.document(i)
        val terms = reader.getTermVector(i, DOC_FIELDS.CONTENT)
        if (terms != null && doc != null){
          var path = doc.get(DOC_FIELDS.PATH)
          val sepIdx = path.lastIndexOf("/")
          if ( sepIdx != -1 ){
            path = path.substring(sepIdx + 1) // only file name
          }
          val dp = new NumericDataPoint(path, Array.empty[Attribute[Double]])
          result += terms
          dps += dp
        }
      }
      (result.toIndexedSeq, dps.toIndexedSeq)
    }
  }



}


object TestDocCluster extends App{
  DocCluster.avergeLinkAlgorithm()
//  println(s"matrix size: ${matrix.length} x ${matrix.length}")
//  println(matrix.map(_.mkString(",")).mkString("\n"))

}