package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import entities.Color
import repositories.ColorRepository
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

import scala.concurrent.ExecutionContext

object ColorJsonProtocol extends DefaultJsonProtocol {
  implicit val colorFormat: RootJsonFormat[Color] = jsonFormat2(Color)
}

object ColorController {
  implicit val system = ActorSystem("color-service")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  import SprayJsonSupport._
  import ColorJsonProtocol._

  val baseUrl = "api" / "v1"

  // Create
  val createRoute =
    pathPrefix(baseUrl) {
      path("colors") {
        post {
          entity(as[Color]) { color =>
            val createdColor = ColorRepository.create(color)
            complete(createdColor)
          }
        }
      }
    }

  // Read
  val getByIdRoute =
    pathPrefix(baseUrl) {
      path("colors" / Segment) { id =>
        get {
          val getByIdResult: Option[Color] = ColorRepository.getById(id.toInt)
          getByIdResult match {
            case Some(color) => complete(color)
            case None => complete(HttpResponse(StatusCodes.NotFound))
          }
        }
      }
    }

  val getAllRoute =
    pathPrefix(baseUrl) {
      path("colors") {
        get {
          val getAllResult = ColorRepository.getAll()
          complete(getAllResult)
        }
      }
    }

  // Update
  val updateRoute =
    pathPrefix(baseUrl) {
      path("colors") {
        put {
          entity(as[Color]) { color =>
            val updateResult = ColorRepository.update(color)
            complete(updateResult)
          }
        }
      }
    }

  // Delete
  val deleteRoute =
    pathPrefix(baseUrl) {
      path("colors" / Segment) { id =>
        delete {
          val deleteResult = ColorRepository.delete(id.toInt)
          complete(HttpResponse(StatusCodes.OK))
        }
      }
    }

  val colorRoutes: Route = createRoute ~ getByIdRoute ~ getAllRoute ~ updateRoute ~ deleteRoute
}
