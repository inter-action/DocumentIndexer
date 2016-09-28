package github.interaction.docsearcher.routing

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import github.interaction.docsearcher.serializers.JsonSupport

import scala.concurrent.{ExecutionContext, Future}

trait LuceneRoute extends Directives with JsonSupport {

  implicit def executionContext: ExecutionContext

  def completeIndex(hasError: Future[Option[Throwable]]): Route =
    onSuccess(hasError){
      case Some(e)=>
        complete(HttpResponse(StatusCodes.ExpectationFailed, entity = HttpEntity(e.toString)))
      case None =>
        complete(HttpResponse(StatusCodes.OK))
    }

}
