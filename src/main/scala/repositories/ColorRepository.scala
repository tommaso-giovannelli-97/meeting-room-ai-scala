package repositories

import com.typesafe.config.ConfigFactory
import entities.{Color, Colors}
import exceptions.NotFoundException
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object ColorRepository {
  val db: Database = {
    val dataSource = new org.postgresql.ds.PGSimpleDataSource()
    dataSource.setUser("postgres")
    dataSource.setPassword("postgres")
    dataSource.setURL("jdbc:postgresql://localhost:5432/meeting-room")

    Database.forDataSource(dataSource, None)
  }

  val colors = TableQuery[Colors]

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2.seconds)

  def create(color: Color): Color = {
    val optionColor : Option[Color] = getByName(color.name)
    if(optionColor.isEmpty) {
      val query = (colors returning colors) += color
      exec(query)
    }
    else{
      throw new Exception("This color already exists.")
    }

  }

  def getById(id: Int): Option[Color] = {
    val query = colors.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getByName(name : String) : Option[Color] = {
    val query = colors.filter(_.name === name)
    exec(query.result.headOption)
  }

  def getAll(): Seq[Color] = {
    exec(colors.result)
  }

  def getAvailable() : Seq[Color] = {
    val allRoomsColorIds = RoomRepository.getAll().map(_.colorId)
    val query = colors.filterNot(_.id.inSet(allRoomsColorIds))
    exec(query.result)
  }

  def update(color: Color): Color = {
    val colorToUpdate: Option[Color] = ColorRepository.getById(color.id.get)
    colorToUpdate match {
      case None => throw new NotFoundException("Color with given id doesn't exist")
      case Some(_) =>
        val query = colors.filter(_.id === color.id).update(color)
        exec(query)
        color
    }
  }

  def delete(id: Int): Int = {
    val colorToDelete: Option[Color] = ColorRepository.getById(id)
    colorToDelete match {
      case None => throw new NotFoundException("Color with given id doesn't exist")
      case Some(_) =>
        val query = colors.filter(_.id === id).delete
        exec(query)
    }
  }
}
