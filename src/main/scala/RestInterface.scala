package github.interaction.docsearcher

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.server.Route

import github.interaction.docsearcher.resources.QuestionResource
import github.interaction.docsearcher.services.QuestionService

trait RestInterface extends Resources {

  implicit def executionContext: ExecutionContext

  lazy val questionService = new QuestionService

  val routes: Route = questionRoutes

}

trait Resources extends QuestionResource

