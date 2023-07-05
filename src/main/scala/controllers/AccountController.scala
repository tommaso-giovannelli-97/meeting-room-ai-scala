package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//import controllers.AccountDTOJsonProtocol.jsonFormat3
//import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import dtos.AccountDTO
import entities.Account
import repositories.AccountRepository
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContext

object AccountJsonProtocol extends DefaultJsonProtocol {
  implicit val accountFormat: RootJsonFormat[Account] = jsonFormat4(Account)
  implicit val accountDTOFormat: RootJsonFormat[AccountDTO] = jsonFormat3(AccountDTO)

}

object AccountController {
  implicit val system = ActorSystem("account-service")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  import SprayJsonSupport._ // Import the SprayJsonSupport trait
  import AccountJsonProtocol._
  //import AccountDTOJsonProtocol._

  val baseUrl = "api" / "v1"

  // Create
  val createRoute =
    pathPrefix(baseUrl) {
      path("accounts") {
        post {
          entity(as[AccountDTO]) { account =>
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
          val getAllResult: Seq[Account] = AccountRepository.getAll()

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

  val accountRoutes: Route = createRoute  ~ getAllRoute ~ getByIdRoute  ~ updateRoute ~ deleteRoute


}


