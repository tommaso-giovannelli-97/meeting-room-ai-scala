package entities

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp
import java.time.LocalDateTime

case class Room(id: Option[Int], colorId: Int, name: String, seats: Int, createdAt: Option[Timestamp], isActive: Option[Boolean]) extends Product with Serializable

class Rooms(tag: Tag) extends Table[Room](tag, "room")  {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def colorId = column[Int]("color_id")
  def name = column[String]("name")
  def seats = column[Int]("seats")
  def createdAt = column[Option[Timestamp]]("created_at", O.Default(Some(Timestamp.valueOf(LocalDateTime.now()))))
  def isActive = column[Option[Boolean]]("is_active", O.Default(Some(true)))

  def * = (id, colorId, name, seats, createdAt, isActive).mapTo[Room]

  def colorIdKey = index("room_color_id_key", colorId, unique = true)
  def nameKey = index("room_name_key", name, unique = true)
}
