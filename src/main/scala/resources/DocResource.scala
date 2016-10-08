package github.interaction.docsearcher.resources

import akka.http.scaladsl.server.Route
import github.interaction.docsearcher.entities._
import github.interaction.docsearcher.routing.LuceneRoute
import github.interaction.docsearcher.services.LuceneService

trait DocResource extends LuceneRoute {

  val luceneService: LuceneService

  def docRoutes: Route = pathPrefix("docs") {
    pathEnd {
      put {
        entity(as[IndexUpdaterModel]) { model =>
          completeIndex(luceneService.createIndex(model))
        }
      } ~
      get {
        parameters('query, 'startPage ? 1, 'itemsPerPage ? 10) { (query, startPage, itemsPerPage)=>
          val result = luceneService.search(QueryModel(query, startPage, itemsPerPage))
          completeOp(result)
        }
      }
    }
  }
}

