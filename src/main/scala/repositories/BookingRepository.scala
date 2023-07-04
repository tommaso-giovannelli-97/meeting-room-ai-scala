package repositories

import com.typesafe.config.ConfigFactory
import dtos.BookingDTO
import entities.{Booking, Bookings}
import slick.jdbc.PostgresProfile.api._

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
    val booking = bookingDTO.toEntity()
    val query = bookings += booking
    exec(query)
    booking
  }

  def getById(id: Int): Option[Booking] = {
    val query = bookings.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(): Seq[Booking] = {
    exec(bookings.result)
  }

  def update(booking: Booking): Booking = {
    val query = bookings.filter(_.id === booking.id).update(booking)
    exec(query)
    booking
  }

  def delete(id: Int): Int = {
    val query = bookings.filter(_.id === id).delete
    exec(query)
  }
}
