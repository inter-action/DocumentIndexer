package github.interaction.docsearcher

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.io.StdIn

object Main extends App with RestInterface {
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem("quiz-management-service")
  implicit val materializer = ActorMaterializer()


  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10 seconds)

  val api = routes


  val bindingFuture = Http().bindAndHandle(handler = api, interface = host, port = port)
  println(s"Server online at http://localhost:5000/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

//  Http().bindAndHandle(handler = api, interface = host, port = port) map { binding =>
//    println(s"REST interface bound to ${binding.localAddress}")
//  } recover { case ex =>
//    println(s"REST interface could not bind to $host:$port ${ex.getMessage}")
//  }

  def terminateAkka(): Unit = {
    system.terminate() onSuccess {
      case _ => println("system exit sucessfully")
    }
  }

//
//  def wayOut(): Unit = {
//
//    val value = Option(StdIn.readLine("print any key to exit:\n"))
//    if (value.isDefined) {
//      terminateAkka()
//    }
//  }
//
//  wayOut()


  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = {
      terminateAkka()
      Thread.sleep(1000) //wait 1 sec for it to exit
    }
  })



}
