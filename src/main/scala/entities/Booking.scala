package entities

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp
import java.time.LocalDateTime

case class Booking(
                    id: Int,
                    roomId: Int,
                    accountId: String,
                    fromTime: Timestamp,
                    untilTime: Timestamp,
                    meetingTitle: String,
                    notes: Option[String],
                    gMeetLink: Option[String],
                    createdAt: Option[Timestamp],
                    isActive: Option[Boolean]
                  ) extends Product with Serializable

class Bookings(tag: Tag) extends Table[Booking](tag, "booking") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def roomId = column[Int]("room_id")

  def accountId = column[String]("account_id")

  def fromTime = column[Timestamp]("from_time")

  def untilTime = column[Timestamp]("until_time")

  def meetingTitle = column[String]("meeting_title")

  def notes = column[Option[String]]("notes")

  def gMeetLink = column[Option[String]]("g_meet_link")

  def createdAt = column[Option[Timestamp]]("created_at", O.Default(Some(Timestamp.valueOf(LocalDateTime.now()))))

  def isActive = column[Option[Boolean]]("is_active", O.Default(Some(true)))

  def * =
    (id, roomId, accountId, fromTime, untilTime, meetingTitle, notes, gMeetLink, createdAt, isActive).mapTo[Booking]

  def fkRoomId =
    foreignKey("booking_room_id_fkey", roomId, TableQuery[Rooms])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)

  def fkAccountId =
    foreignKey("booking_account_id_fkey", accountId, TableQuery[Accounts])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
}

