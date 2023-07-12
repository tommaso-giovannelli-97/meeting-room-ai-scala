package repositories

import com.typesafe.config.ConfigFactory
import dtos.RoomDTO
import entities.{Room, Rooms}
import exceptions.NotFoundException
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

  def update(id: Int, room: Room): Room = {
    val roomToUpdate: Option[Room] = RoomRepository.getById(id)
    roomToUpdate match {
      case None => throw new NotFoundException("Room with given id doesn't exist")
      case Some(_) =>
        val query = rooms.filter(_.id === id).update(room)
        exec(query)
        val updatedRoom: Option[Room] = RoomRepository.getById(id)
        updatedRoom.getOrElse(throw new NotFoundException("Room with given id doesn't exist"))
    }
  }

  def delete(id: Int): Int = {
    val roomToDelete: Option[Room] = RoomRepository.getById(id)
    roomToDelete match {
      case None => throw new NotFoundException("Room with given id doesn't exist")
      case Some(_) =>
        val query = rooms.filter(_.id === id).delete
        exec(query)
    }
  }
}
