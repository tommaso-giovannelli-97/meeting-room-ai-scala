package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import dtos.{BookingDTO, BookingOccupationDTO, FilterDTO}
import entities.Booking
import exceptions.{NotFoundException, OverlappingBookingException}
import repositories.BookingRepository
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}
import utils.DateTimeUtils

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
  implicit val filterDTO : RootJsonFormat[FilterDTO] = jsonFormat4(FilterDTO)
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
            try {
              val createdBooking = BookingRepository.create(booking)
              complete(createdBooking)
            } catch {
              case ex: OverlappingBookingException => complete(HttpResponse(StatusCodes.BadRequest, entity = ex.getMessage))
              case ex: NotFoundException => complete(HttpResponse(StatusCodes.NotFound, entity = ex.getMessage))
            }
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
          parameters("page".as[Int].?, "size".as[Int].?) {
            (pageOpt, sizeOpt) =>
            val page = pageOpt.getOrElse(0)
            val size = sizeOpt.getOrElse(5)

            val getAllResult = BookingRepository.getAll(page, size)
            complete(getAllResult)
          }
        }
      }
    }

  val getAllByAccountIdRoute =
    pathPrefix(baseUrl) {
      path("accounts" / Segment / "bookings") { accountId =>
        get {
          parameters("page".as[Int].?, "size".as[Int].?) {
            (pageOpt, sizeOpt) =>
              val page = pageOpt.getOrElse(0)
              val size = sizeOpt.getOrElse(5)
              try {
                val bookings: Seq[Booking] = BookingRepository.getAllByAccountId(accountId, page, size)
                complete(bookings)
              } catch {
                case ex : NotFoundException => complete(HttpResponse(StatusCodes.NotFound, entity = ex.getMessage))
              }
          }
        }
      }
    }

  val getUpcomingRoute =
    pathPrefix(baseUrl) {
      path("bookings" / "upcoming") {
        get {
          parameters("page".as[Int].?, "size".as[Int].?) {
            (pageOpt, sizeOpt) =>
              val page = pageOpt.getOrElse(0)
              val size = sizeOpt.getOrElse(5)

              val bookings: Seq[Booking] = BookingRepository.getUpcoming(page, size)
              complete(bookings)
          }
        }
      }
    }

  val getByDateRoute =
    pathPrefix(baseUrl) {
      path("bookings" / "date") {
        parameters("date") { (date) =>
          get {
            parameters("page".as[Int].?, "size".as[Int].?) {
              (pageOpt, sizeOpt) =>
                val page = pageOpt.getOrElse(0)
                val size = sizeOpt.getOrElse(5)

                val bookings: Seq[Booking] = BookingRepository.getByDate(date, page, size)
                complete(bookings)
            }
          }
        }
      }
    }

  val getRoomOccupationBetweenDatesRoute =
    pathPrefix(baseUrl) {
      path("bookings" / Segment / Segment / Segment) { (roomName, startDate, endDate) =>
        get {
          try {
            val occupationResult: Seq[BookingOccupationDTO] = BookingRepository.getRoomOccupationBetweenDates(roomName, startDate, endDate)
            complete(occupationResult)
          } catch {
            case ex: NotFoundException => complete(HttpResponse(StatusCodes.NotFound, entity = ex.getMessage))
          }

        }
      }
    }

val getFilteredBookingsRoute =
  pathPrefix(baseUrl) {
    path("filters") {
      post {
        entity(as[FilterDTO]) { filters =>
          try {
            val startDate : Option[Timestamp] = DateTimeUtils.convertOptionalStringToTimestamp(filters.fromTime)
            val endDate : Option[Timestamp] = DateTimeUtils.convertOptionalStringToTimestamp(filters.untilTime)

            val bookings: Seq[Booking] = BookingRepository.getFilteredBookings(filters.roomId, filters.accountId, startDate, endDate)
            complete(bookings)

          } catch {
            case ex: NotFoundException => complete(HttpResponse(StatusCodes.NotFound, entity = ex.getMessage))
          }
        }
      }
    }
  }



  // Update
  val updateRoute =
    pathPrefix(baseUrl) {
      path("bookings" / Segment) {id =>
        put {
          entity(as[Booking]) { booking =>
            try {
              val updateResult = BookingRepository.update(id.toInt, booking)
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
      path("bookings" / Segment) { id =>
        delete {
          try {
            val deleteResult = BookingRepository.delete(id.toInt)
            complete(HttpResponse(StatusCodes.OK))
          } catch {
            case ex: NotFoundException => complete(HttpResponse(StatusCodes.NotFound, entity = ex.getMessage))
          }
        }
      }
    }

  //~ getAllRoute
  val bookingRoutes: Route = createRoute ~ getAllRoute ~ getUpcomingRoute ~ getByDateRoute ~
    getFilteredBookingsRoute ~ getRoomOccupationBetweenDatesRoute ~ getByIdRoute ~ getAllByAccountIdRoute  ~ updateRoute ~ deleteRoute
}
