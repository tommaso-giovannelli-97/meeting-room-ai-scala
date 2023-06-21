package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import entities.Account
import repositories.AccountRepository
import slick.jdbc.MySQLProfile.api._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object AccountJsonProtocol extends DefaultJsonProtocol {
  implicit val accountFormat: RootJsonFormat[Account] = jsonFormat4(Account)
}

object AccountController {
  implicit val system = ActorSystem("account-service")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  import SprayJsonSupport._ // Import the SprayJsonSupport trait
  import AccountJsonProtocol._

  val baseUrl = "api" / "v1"

  // Create
  val createRoute =
    pathPrefix(baseUrl) {
      path("accounts") {
        post {
          entity(as[Account]) { account =>
            val createdAccount = AccountRepository.create(account)

            complete(createdAccount)

          }
        }
      }
    }

  // Read
  val getByIdRoute =
    pathPrefix(baseUrl) {
      path("accounts" / Segment) { id =>
        get {
          val getByIdResult: Option[Account] = AccountRepository.getById(id)
          getByIdResult match {
            case Some(account) => complete(account)
            case None => complete(HttpResponse(StatusCodes.NotFound))
          }
        }
      }
    }

  val getAllRoute =
    pathPrefix(baseUrl) {
      path("accounts") {
        get {
          val getAllResult = AccountRepository.getAll()

          complete(getAllResult)
        }
      }
    }

  // Update
  val updateRoute =
    pathPrefix(baseUrl) {
      path("accounts") {
        put {
          entity(as[Account]) { account =>
            val updateResult = AccountRepository.update(account)
            complete(updateResult)
          }
        }
      }
    }

  // Delete
  val deleteRoute =
    pathPrefix(baseUrl) {
      path("accounts" / Segment) { id =>
        delete {
          val deleteResult = AccountRepository.delete(id)
          complete(HttpResponse(StatusCodes.OK))
        }
      }
    }

  val routes: Route = createRoute ~ getByIdRoute ~ getAllRoute ~ updateRoute ~ deleteRoute

}


