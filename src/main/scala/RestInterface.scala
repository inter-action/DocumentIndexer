package github.interaction.docsearcher

import akka.http.scaladsl.server.Route
import github.interaction.docsearcher.resources.{DocResource, QuestionResource}
import github.interaction.docsearcher.services.{LuceneService, QuestionService}

import scala.concurrent.ExecutionContext

trait RestInterface extends MySource {

  implicit def executionContext: ExecutionContext

  lazy val questionService = new QuestionService
  lazy val luceneService = new LuceneService()

  val routes: Route = docRoutes ~ questionRoutes

}

trait MySource extends DocResource with QuestionResource


