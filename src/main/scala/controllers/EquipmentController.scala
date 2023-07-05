package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import entities.Equipment
import repositories.EquipmentRepository
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

import java.sql.Timestamp
import scala.concurrent.ExecutionContext

object EquipmentJsonProtocol extends DefaultJsonProtocol {
  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    def write(obj: Timestamp): JsValue = JsString(obj.toString)
    def read(json: JsValue): Timestamp = json match {
      case JsString(s) => Timestamp.valueOf(s)
      case _           => throw DeserializationException("Expected Timestamp as JsString")
    }
  }

  implicit val equipmentFormat: RootJsonFormat[Equipment] = jsonFormat4(Equipment)
}

object EquipmentController {
  implicit val system = ActorSystem("equipment-service")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  import SprayJsonSupport._
  import EquipmentJsonProtocol._

  val baseUrl = "api" / "v1"

  // Create
  val createRoute =
    pathPrefix(baseUrl) {
      path("equipment") {
        post {
          entity(as[Equipment]) { equipment =>
            val createdEquipment = EquipmentRepository.create(equipment)
            complete(createdEquipment)
          }
        }
      }
    }

  // Read
  val getByIdRoute =
    pathPrefix(baseUrl) {
      path("equipment" / Segment) { id =>
        get {
          val getByIdResult: Option[Equipment] = EquipmentRepository.getById(id.toInt)
          getByIdResult match {
            case Some(equipment) => complete(equipment)
            case None => complete(HttpResponse(StatusCodes.NotFound))
          }
        }
      }
    }

  val getAllRoute =
    pathPrefix(baseUrl) {
      path("equipment") {
        get {
          val getAllResult = EquipmentRepository.getAll()
          complete(getAllResult)
        }
      }
    }

  // Update
  val updateRoute =
    pathPrefix(baseUrl) {
      path("equipment") {
        put {
          entity(as[Equipment]) { equipment =>
            val updateResult = EquipmentRepository.update(equipment)
            complete(updateResult)
          }
        }
      }
    }

  // Delete
  val deleteRoute =
    pathPrefix(baseUrl) {
      path("equipment" / Segment) { id =>
        delete {
          val deleteResult = EquipmentRepository.delete(id.toInt)
          complete(HttpResponse(StatusCodes.OK))
        }
      }
    }

  val equipmentRoutes: Route = createRoute ~ getByIdRoute ~ getAllRoute ~ updateRoute ~ deleteRoute
}
