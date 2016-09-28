package github.interaction.docsearcher.resources

import akka.http.scaladsl.server.Route
import github.interaction.docsearcher.entities.{Question, QuestionUpdate}
import github.interaction.docsearcher.routing.QuestionRoute
import github.interaction.docsearcher.services.QuestionService

trait QuestionResource extends QuestionRoute {

  val questionService: QuestionService

  def questionRoutes: Route = pathPrefix("questions") {
    pathEnd {
      post {
        entity(as[Question]) { question =>
          completeWithLocationHeader(
            resourceId = questionService.createQuestion(question),
            ifDefinedStatus = 201, ifEmptyStatus = 409)
          }
        }
    } ~
    path(Segment) { id =>
      get {
        complete(questionService.getQuestion(id))
      } ~
      put {
        entity(as[QuestionUpdate]) { update =>
          complete(questionService.updateQuestion(id, update))
        }
      } ~
      delete {
        complete(questionService.deleteQuestion(id))
      }
    }

  }
}

