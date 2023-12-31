package repositories

import dtos.{BookingDTO, BookingOccupationDTO}
import entities.{Booking, Bookings, Room}
import exceptions.{NotFoundException, OverlappingBookingException}
import slick.jdbc.PostgresProfile.api._
import utils.DateTimeUtils

import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object BookingRepository {
  val db: Database = {
    val dataSource = new org.postgresql.ds.PGSimpleDataSource()
    dataSource.setUser("postgres")
    dataSource.setPassword("postgres")
    dataSource.setURL("jdbc:postgresql://localhost:5432/meeting-room")

    Database.forDataSource(dataSource, None)
  }

  val bookings = TableQuery[Bookings]

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2.seconds)

  def create(bookingDTO: BookingDTO): Booking = {
    val optionRoom: Option[Room] = RoomRepository.getById(bookingDTO.roomId)
    if (optionRoom.isEmpty) {
      throw new NotFoundException(("Room with this id doesn't exist"))
    }
    if (isBookingOverlapping(bookingDTO.roomId, bookingDTO.fromTime, bookingDTO.untilTime)) {
      throw new OverlappingBookingException("Overlapping Booking")
    }
    val booking = bookingDTO.toEntity()
    val query = (bookings returning bookings) += booking
    exec(query)
  }

  def getById(id: Int): Option[Booking] = {
    val query = bookings.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(page:Int, size:Int): Seq[Booking] = {
    val query = bookings.drop(page * size).take(size)
    exec(query.result)
  }

  def getAllByAccountId(accountId : String, page:Int, size:Int) : Seq[Booking] = {
    val account = AccountRepository.getById(accountId)
    account match {
      case None => throw new NotFoundException("Account with given id doesn't exist")
      case Some(_) =>
        val query = bookings.filter(_.accountId === accountId).drop(page * size).take(size)
        exec(query.result)
    }
  }

  def getUpcoming(page:Int, size:Int) : Seq[Booking] = {
    val currentDateTime = new Timestamp(System.currentTimeMillis())
    val query= bookings.filter(_.fromTime > currentDateTime).drop(page * size).take(size)
    exec(query.result)
  }

  def getByDate(stringDate : String, page:Int, size:Int): Seq[Booking] = {
    val date : Timestamp = DateTimeUtils.convertStringToTimestamp(stringDate).get
    val startDayDate = DateTimeUtils.toBeginningOfDay(date)
    val endDayDate = DateTimeUtils.toEndOfDay(date)

    val query = bookings.filter(b => b.fromTime >=startDayDate && b.fromTime<=endDayDate).drop(page * size).take(size)
    exec(query.result)
  }

  def getByRoomId(roomId: Int, page:Int, size:Int): Seq[Booking] = {
    val query = bookings.filter(b => b.roomId === roomId).drop(page * size).take(size)
    exec(query.result)
  }

  def getByRoomIdBetweenDates(roomId : Int, startDate: Timestamp, endDate: Timestamp) : Seq[Booking] = {
    val query = bookings.filter(b => b.roomId===roomId &&  b.fromTime >=startDate && b.fromTime<=endDate)
    exec(query.result)
  }

  def getFilteredBookings(roomId: Option[Int],
                          accountId: Option[String],
                          fromDate: Option[Timestamp],
                          untilDate: Option[Timestamp]) : Seq[Booking] = {
    val query = bookings
      .filter(b =>
        roomId.map(id => b.roomId === id).getOrElse(true: Rep[Boolean]) &&
          accountId.map(id => b.accountId === id).getOrElse(true: Rep[Boolean]) &&
          fromDate.map(date => b.fromTime >= date).getOrElse(true: Rep[Boolean]) &&
          untilDate.map(date => b.untilTime <= date).getOrElse(true: Rep[Boolean]) &&
          b.isActive === true
      )

    exec(query.result)
  }

  def getRoomOccupationBetweenDates(roomName: String, startDate:String, endDate:String)
  :Seq[BookingOccupationDTO] = {
    val optionRoom : Option[Room] = RoomRepository.getByNameIgnoreCase(roomName)
    optionRoom match {
      case None => throw new NotFoundException("Room with given id doesn't exist")
      case Some(_) =>
        val startDateTimestamp: Timestamp = DateTimeUtils.toWorkDayBeginning(DateTimeUtils.convertStringToTimestamp(startDate).get)
        val endDateTimestamp: Timestamp = DateTimeUtils.toWorkDayEnd(DateTimeUtils.convertStringToTimestamp(endDate).get)
        val bookingsBetweenDates: Seq[Booking] = getByRoomIdBetweenDates(optionRoom.get.id.get, startDateTimestamp, endDateTimestamp)
        var allOccupationSlots: Vector[BookingOccupationDTO] = generateBookingOccupationSlots(startDateTimestamp, endDateTimestamp)
        for (booking <- bookingsBetweenDates) {
          val bookingSlots: Range.Inclusive = getBookingOccupationSlots(startDateTimestamp, booking.fromTime, booking.untilTime)
          for (slot <- bookingSlots) {
            allOccupationSlots = allOccupationSlots.updated(slot, getUpdatedOccupationDTO(allOccupationSlots(slot)))
          }
        }
        allOccupationSlots
    }

  }

  def isBookingOverlapping(roomId: Int, startDate: Timestamp, endDate:Timestamp) : Boolean = {
    val query = bookings.filter(b => b.roomId === roomId &&( b.fromTime >= startDate && b.fromTime <= endDate) ||
      (b.untilTime >= startDate && b.untilTime <= endDate) || (b.fromTime <= startDate && b.untilTime >= endDate))
    val overlappingBookings = exec(query.result)
    overlappingBookings.nonEmpty
  }

  def update(id: Int, booking: Booking): Booking = {
    val bookingToUpdate: Option[Booking] = BookingRepository.getById(id)
    bookingToUpdate match {
      case None => throw new NotFoundException("Booking with given id doesn't exist")
      case Some(_) =>
        val query = bookings.filter(_.id === id).update(booking)
        exec(query)
        val updatedBooking: Option[Booking] = BookingRepository.getById(id)
        updatedBooking.getOrElse(throw new NotFoundException("Booking with given id doesn't exist"))
    }
  }

  def delete(id: Int): Int = {
    val bookingToDelete: Option[Booking] = BookingRepository.getById(id)
    bookingToDelete match {
      case None => throw new NotFoundException("Booking with given id doesn't exist")
      case Some(_) =>
        val query = bookings.filter(_.id === id).delete
        exec(query)
    }


  }

  //--------------Support services -------------------
 def generateBookingOccupationSlots(startDate : Timestamp, endDate: Timestamp) : Vector[BookingOccupationDTO] = {
   var occupationSlots : Vector[BookingOccupationDTO] = Vector()

   for (i <- 0 until DateTimeUtils.getDaysDifferencebetweenDates(startDate, endDate) + 1) {
     for (j <- 0 until 36) {
       occupationSlots = occupationSlots :+ generateOccupationSlot(startDate, i, j)
     }
   }
   occupationSlots

 }

  def generateOccupationSlot(startDate: Timestamp, daysDifference: Int, slotNumber: Int): BookingOccupationDTO = {
    var fromDate: Timestamp = DateTimeUtils.addDaysToTimestamp(startDate, daysDifference)
    fromDate = DateTimeUtils.addMinutesToTimestamp(fromDate, slotNumber*15)
    val toDate : Timestamp = DateTimeUtils.addMinutesToTimestamp(fromDate, 15)

    BookingOccupationDTO(fromDate.toLocalDateTime.toLocalDate
      ,fromDate.getTime.toString, toDate.getTime.toString, false)
  }

  def getBookingOccupationSlots(startDate: Timestamp, bookingStartTime: Timestamp, bookingEndTime: Timestamp): Range.Inclusive = {
    val startIndex: Int = (DateTimeUtils.getDaysDifferencebetweenDates(startDate, bookingStartTime) * 36) +
      DateTimeUtils.getMinutesDifferenceBetweenDates(startDate, bookingStartTime) / 15

    val endIndex: Int = startIndex + (DateTimeUtils.getMinutesDifferenceBetweenDates(bookingStartTime, bookingEndTime) / 15 )
    startIndex to endIndex
  }

  def getUpdatedOccupationDTO(bookingOccupationDTO: BookingOccupationDTO) : BookingOccupationDTO = {
    bookingOccupationDTO.copy(isOccupied = !bookingOccupationDTO.isOccupied)
  }

}
