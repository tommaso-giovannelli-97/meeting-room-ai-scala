import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import controllers.AccountController.accountRoutes
import controllers.BookingController.bookingRoutes
import controllers.ColorController.colorRoutes
import controllers.EquipmentController.equipmentRoutes
import controllers.RoomController.roomRoutes
import controllers.RoomSetupController.roomSetupRoutes

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("demo-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  val appRoutes : Route =  colorRoutes ~ accountRoutes ~ bookingRoutes ~
    equipmentRoutes ~ roomRoutes ~ roomSetupRoutes

  val bindingFuture = Http().newServerAt("localhost", 8081).bind(appRoutes)

  bindingFuture.onComplete {
    case Success(binding) =>
      println(s"Server online at http://localhost:8081/")
    case Failure(ex) =>
      println(s"Server could not start!")
      ex.printStackTrace()
  }

}
