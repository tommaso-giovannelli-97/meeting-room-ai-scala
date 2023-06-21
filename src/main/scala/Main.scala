import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import controllers.AccountController.routes

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.{Failure, Success}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("demo-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  //val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

  //println("Server online. Press enter to stop.")
  //StdIn.readLine()

  bindingFuture.onComplete {
    case Success(binding) =>
      println(s"Server online at http://localhost:8080/")
    case Failure(ex) =>
      println(s"Server could not start!")
      ex.printStackTrace()
  }

}
