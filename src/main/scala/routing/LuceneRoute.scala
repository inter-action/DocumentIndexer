package github.interaction.docsearcher.routing

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import github.interaction.docsearcher.serializers.{JsonSupport, MyJsonSupport}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/*
complete 参数是 m => ToResponseMarshallable

官方默认的定义在这里
http://doc.akka.io/docs/akka/2.4.11/scala/http/common/marshalling.html
 */
trait LuceneRoute extends Directives with JsonSupport {

  implicit def executionContext: ExecutionContext

  def completeIndex(hasError: Future[Try[Unit]]): Route =
    onSuccess(hasError){
      case Success(())=>
        complete("")
      case Failure(ex) =>
        complete((StatusCodes.ExpectationFailed, ex.getMessage))
    }

  def completeOp[T: ToEntityMarshaller](result: Future[Try[T]]): Route =
    onSuccess(result){
      case Success(t)=>
        complete((StatusCodes.OK, t))
      case Failure(ex) =>
        complete((StatusCodes.ExpectationFailed, ex.getMessage))
    }
}
