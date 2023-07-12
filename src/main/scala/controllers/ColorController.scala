package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import entities.Color
import exceptions.NotFoundException
import repositories.ColorRepository
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

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
            try {
              val createdColor = ColorRepository.create(color)
              complete(createdColor)
            } catch {
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

  val getAvailableRoute =
    pathPrefix(baseUrl) {
      path("colors" / "available") {
        get {
          val getAvailableResult = ColorRepository.getAvailable()
          complete(getAvailableResult)
        }
      }
    }

  // Update
  val updateRoute =
    pathPrefix(baseUrl) {
      path("colors"/ Segment) {id =>
        put {
          entity(as[Color]) { color =>
            try {
              val updateResult = ColorRepository.update(id.toInt, color)
              complete(updateResult)
            } catch {
              case ex: NotFoundException => complete(HttpResponse(StatusCodes.NotFound, entity = ex.getMessage))
            }
          }
        }
      }
    }

  // Delete
  val deleteRoute =
    pathPrefix(baseUrl) {
      path("colors" / Segment) { id =>
        delete {
          try {
            val deleteResult = ColorRepository.delete(id.toInt)
            complete(HttpResponse(StatusCodes.OK))
          } catch {
            case ex: NotFoundException => complete(HttpResponse(StatusCodes.NotFound, entity = ex.getMessage))
          }
        }
      }
    }

  val colorRoutes: Route = createRoute ~ getAvailableRoute ~ getByIdRoute ~ getAllRoute ~ updateRoute ~ deleteRoute
}
