package github.interaction.docsearcher.entities

//case class
case class IndexUpdaterModel(indexPath: Option[String], docPath: String, isUpdate: Option[Boolean])

case class ResponseEntity[T](data: T)


case class QueryModel(
                       query: String,
                       startPage: Option[Int] = Some(1),
                       itemsPerPage: Option[Int] = Some(10)
                     ){

  val _startPage = startPage.get
  val _itemsPerPage = itemsPerPage.get

  // total rows to fetch
  def total = _startPage * _itemsPerPage
  // include
  def offsetStart = (_startPage - 1) * _itemsPerPage
  // exclude
  def endOffset = _startPage * _itemsPerPage
}

case class PaginationResult[T](
                        data: List[T],
                        total: Int
                        )

case class DocumentResult(
                         path: String
                         )