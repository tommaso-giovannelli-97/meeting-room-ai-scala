package repositories

import com.typesafe.config.ConfigFactory
import entities.{Room, Rooms}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object RoomRepository {
  val config = ConfigFactory.load()
  val db = Database.forConfig("postgresConnection", config)

  val rooms = TableQuery[Rooms]

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2.seconds)

  def create(room: Room): Room = {
    val query = rooms += room
    exec(query)
    room
  }

  def getById(id: Int): Option[Room] = {
    val query = rooms.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(): Seq[Room] = {
    exec(rooms.result)
  }

  def update(room: Room): Room = {
    val query = rooms.filter(_.id === room.id).update(room)
    exec(query)
    room
  }

  def delete(id: Int): Int = {
    val query = rooms.filter(_.id === id).delete
    exec(query)
  }
}
