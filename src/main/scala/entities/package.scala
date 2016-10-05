package github.interaction.docsearcher.entities

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}


//case class
case class IndexUpdaterModel(indexPath: Option[String], docPath: String, isUpdate: Option[Boolean])

case class ResponseEntity[T](data: T)


case class QueryModel(
                       query: String,
                       startPage: Int = 1,
                       itemsPerPage: Int = 10
                     ) {


  // total rows to fetch
  def total = startPage * itemsPerPage

  // include
  def offsetStart = (startPage - 1) * itemsPerPage

  // exclude
  def endOffset = startPage * itemsPerPage
}

case class PaginationResult[T](
                                data: List[T],
                                total: Int
                              )

case class DocumentResult(
                           path: String
                         )