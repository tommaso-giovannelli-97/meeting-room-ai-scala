package repositories

import com.typesafe.config.ConfigFactory
import dtos.RoomDTO
import entities.{Room, Rooms}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object RoomRepository {
  val db: Database = {
    val dataSource = new org.postgresql.ds.PGSimpleDataSource()
    dataSource.setUser("postgres")
    dataSource.setPassword("postgres")
    dataSource.setURL("jdbc:postgresql://localhost:5432/meeting-room")

    Database.forDataSource(dataSource, None)
  }

  val rooms = TableQuery[Rooms]

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2.seconds)

  def create(roomDTO: RoomDTO): Room = {
    val optionRoom : Option[Room] = getByNameIgnoreCase(roomDTO.name)
    if(optionRoom.isEmpty) {
      val room = roomDTO.toEntity()
      val query = (rooms returning rooms) += room
      exec(query)
    }
    else {
      throw new Exception("A room with this name already exists.")
    }

  }

  def getById(id: Int): Option[Room] = {
    val query = rooms.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getByNameIgnoreCase(name : String): Option[Room] = {
    val query = rooms.filter(_.name.toLowerCase like name.toLowerCase)
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
