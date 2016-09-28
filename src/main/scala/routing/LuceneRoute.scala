package github.interaction.docsearcher.routing

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import github.interaction.docsearcher.serializers.JsonSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait LuceneRoute extends Directives with JsonSupport {

  implicit def executionContext: ExecutionContext

  def completeIndex(hasError: Future[Try[Unit]]): Route =
    onSuccess(hasError){
      case Success(())=>
        complete(HttpResponse(StatusCodes.OK))
      case Failure(ex) =>
        complete(HttpResponse(StatusCodes.ExpectationFailed, entity = HttpEntity(ex.getMessage)))
    }

}
