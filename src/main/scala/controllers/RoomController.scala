package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import dtos.RoomDTO
import entities.Room
import repositories.RoomRepository
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

import java.sql.Timestamp
import scala.concurrent.ExecutionContext

object RoomJsonProtocol extends DefaultJsonProtocol {
  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    def write(obj: Timestamp): JsValue = JsString(obj.toString)

    def read(json: JsValue): Timestamp = json match {
      case JsString(s) => Timestamp.valueOf(s)
      case _ => throw new DeserializationException("Expected Timestamp as JsString")
    }
  }

  implicit val roomFormat: RootJsonFormat[Room] = jsonFormat6(Room)
  implicit val roomDTOFormat: RootJsonFormat[RoomDTO] = jsonFormat3(RoomDTO)
}

object RoomController {
  implicit val system = ActorSystem("room-service")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  import SprayJsonSupport._
  import RoomJsonProtocol._

  val baseUrl = "api" / "v1"

  // Create
  val createRoute =
    pathPrefix(baseUrl) {
      path("rooms") {
        post {
          entity(as[RoomDTO]) { room =>
            try {
              val createdRoom = RoomRepository.create(room)
              complete(createdRoom)
            }
            catch {
              case ex: Exception =>
                complete(StatusCodes.BadRequest, s"Error occurred: ${ex.getMessage}")
            }
          }
        }
      }
    }

  // Read
  val getByIdRoute =
    pathPrefix(baseUrl) {
      path("rooms" / Segment) { id =>
        get {
          val getByIdResult: Option[Room] = RoomRepository.getById(id.toInt)
          getByIdResult match {
            case Some(room) => complete(room)
            case None => complete(HttpResponse(StatusCodes.NotFound))
          }
        }
      }
    }

  val getAllRoute =
    pathPrefix(baseUrl) {
      path("rooms") {
        get {
          val getAllResult = RoomRepository.getAll()
          complete(getAllResult)
        }
      }
    }

  // Update
  val updateRoute =
    pathPrefix(baseUrl) {
      path("rooms") {
        put {
          entity(as[Room]) { room =>
            val updateResult = RoomRepository.update(room)
            complete(updateResult)
          }
        }
      }
    }

  // Delete
  val deleteRoute =
    pathPrefix(baseUrl) {
      path("rooms" / Segment) { id =>
        delete {
          val deleteResult = RoomRepository.delete(id.toInt)
          complete(HttpResponse(StatusCodes.OK))
        }
      }
    }

  val roomRoutes: Route = createRoute ~ getByIdRoute ~ getAllRoute ~ updateRoute ~ deleteRoute
}
