package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import dtos.RoomSetupDTO
import entities.RoomSetup
import repositories.RoomSetupRepository
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContext

object RoomSetupJsonProtocol extends DefaultJsonProtocol {
  implicit val roomSetupFormat: RootJsonFormat[RoomSetup] = jsonFormat4(RoomSetup)
  implicit val roomSetupDTOFormat: RootJsonFormat[RoomSetupDTO] = jsonFormat2(RoomSetupDTO)
}

object RoomSetupController {
  implicit val system = ActorSystem("room-setup-service")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  import SprayJsonSupport._
  import RoomSetupJsonProtocol._

  val baseUrl = "api" / "v1"

  // Create
  val createRoute =
    pathPrefix(baseUrl) {
      path("room-setups") {
        post {
          entity(as[RoomSetupDTO]) { roomSetup =>
            val createdRoomSetup = RoomSetupRepository.create(roomSetup)
            complete(createdRoomSetup)
          }
        }
      }
    }

  // Read
  val getByIdRoute =
    pathPrefix(baseUrl) {
      path("room-setups" / Segment) { id =>
        get {
          val getByIdResult: Option[RoomSetup] = RoomSetupRepository.getById(id.toInt)
          getByIdResult match {
            case Some(roomSetup) => complete(roomSetup)
            case None => complete(HttpResponse(StatusCodes.NotFound))
          }
        }
      }
    }

  val getAllRoute =
    pathPrefix(baseUrl) {
      path("room-setups") {
        get {
          val getAllResult = RoomSetupRepository.getAll()
          complete(getAllResult)
        }
      }
    }

  // Update
  val updateRoute =
    pathPrefix(baseUrl) {
      path("room-setups") {
        put {
          entity(as[RoomSetup]) { roomSetup =>
            val updateResult = RoomSetupRepository.update(roomSetup)
            complete(updateResult)
          }
        }
      }
    }

  // Delete
  val deleteRoute =
    pathPrefix(baseUrl) {
      path("room-setups" / Segment) { id =>
        delete {
          val deleteResult = RoomSetupRepository.delete(id.toInt)
          complete(HttpResponse(StatusCodes.OK))
        }
      }
    }

  val roomSetupRoutes: Route = createRoute ~ getByIdRoute ~ getAllRoute ~ updateRoute ~ deleteRoute
}
