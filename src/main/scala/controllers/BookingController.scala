package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import dtos.{BookingDTO, BookingOccupationDTO}
import entities.Booking
import repositories.BookingRepository
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

import java.sql.Timestamp
import java.time.LocalDate
import scala.concurrent.ExecutionContext

object BookingJsonProtocol extends DefaultJsonProtocol {
  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    def write(obj: Timestamp): JsValue = JsString(obj.toString)

    def read(json: JsValue): Timestamp = json match {
      case JsString(s) => Timestamp.valueOf(s)
      case _ => throw new DeserializationException("Expected Timestamp as JsString")
    }
  }

  implicit object LocalDateFormat extends JsonFormat[LocalDate] {
    def write(obj: LocalDate): JsValue = JsString(obj.toString)

    def read(json: JsValue): LocalDate = json match {
      case JsString(s) => LocalDate.parse(s)
      case _ => throw DeserializationException("Expected LocalDate as JsString")
    }
  }

  implicit val bookingFormat: RootJsonFormat[Booking] = jsonFormat10(Booking)
  implicit val bookingDTOFormat: RootJsonFormat[BookingDTO] = jsonFormat7(BookingDTO)
  implicit val bookingOccupationDTOFormat: RootJsonFormat[BookingOccupationDTO] = jsonFormat4(BookingOccupationDTO)
}

object BookingController {
  implicit val system = ActorSystem("booking-service")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  import SprayJsonSupport._
  import BookingJsonProtocol._

  val baseUrl = "api" / "v1"

  // Create
  val createRoute =
    pathPrefix(baseUrl) {
      path("bookings") {
        post {
          entity(as[BookingDTO]) { booking =>
            val createdBooking = BookingRepository.create(booking)
            complete(createdBooking)
          }
        }
      }
    }

  // Read
  val getByIdRoute =
    pathPrefix(baseUrl) {
      path("bookings" / Segment) { id =>
        get {
          val getByIdResult: Option[Booking] = BookingRepository.getById(id.toInt)
          getByIdResult match {
            case Some(booking) => complete(booking)
            case None => complete(HttpResponse(StatusCodes.NotFound))
          }
        }
      }
    }

  val getAllRoute =
    pathPrefix(baseUrl) {
      path("bookings") {
        get {
          val getAllResult = BookingRepository.getAll()
          complete(getAllResult)
        }
      }
    }

  val getAllByAccountIdRoute =
    pathPrefix(baseUrl) {
      path("accounts" / Segment / "bookings") { accountId =>
        get {
          val bookings: Seq[Booking] = BookingRepository.getAllByAccountId(accountId)
          complete(bookings)
        }
      }
    }

  val getUpcomingRoute =
    pathPrefix(baseUrl) {
      path("bookings" / "upcoming") {
        get {
          val bookings: Seq[Booking] = BookingRepository.getUpcoming()
          complete(bookings)
        }
      }
    }

  val getByDateRoute =
    pathPrefix(baseUrl) {
      path("bookings" / "date") {
        parameters("date") { (date) =>
          get {
            val bookings: Seq[Booking] = BookingRepository.getByDate(date)
            complete(bookings)
          }
        }
      }
    }

  val getRoomOccupationBetweenDatesRoute =
    pathPrefix(baseUrl) {
      path("bookings" / Segment / Segment / Segment) { (roomName, startDate, endDate) =>
        get {
          val occupationResult: Seq[BookingOccupationDTO] = BookingRepository.getRoomOccupationBetweenDates(roomName, startDate, endDate)
          complete(occupationResult)
        }
      }
    }

  // Update
  val updateRoute =
    pathPrefix(baseUrl) {
      path("bookings") {
        put {
          entity(as[Booking]) { booking =>
            val updateResult = BookingRepository.update(booking)
            complete(updateResult)
          }
        }
      }
    }

  // Delete
  val deleteRoute =
    pathPrefix(baseUrl) {
      path("bookings" / Segment) { id =>
        delete {
          val deleteResult = BookingRepository.delete(id.toInt)
          complete(HttpResponse(StatusCodes.OK))
        }
      }
    }

  //~ getAllRoute
  val bookingRoutes: Route = createRoute ~ getAllRoute ~ getUpcomingRoute ~ getByDateRoute ~
    getRoomOccupationBetweenDatesRoute ~ getByIdRoute ~ getAllByAccountIdRoute  ~ updateRoute ~ deleteRoute
}
