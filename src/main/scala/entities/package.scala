package github.interaction.docsearcher.entities

//case class
case class IndexUpdaterModel(indexPath: Option[String], docPath: String, isUpdate: Option[Boolean])

case class ResponseEntity[T](data: T)