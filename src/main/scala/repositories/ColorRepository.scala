package repositories

import com.typesafe.config.ConfigFactory
import entities.{Color, Colors}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object ColorRepository {
  val config = ConfigFactory.load()
  val db = Database.forConfig("postgresConnection", config)

  val colors = TableQuery[Colors]

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2.seconds)

  def create(color: Color): Color = {
    val query = colors += color
    exec(query)
    color
  }

  def getById(id: Int): Option[Color] = {
    val query = colors.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(): Seq[Color] = {
    exec(colors.result)
  }

  def update(color: Color): Color = {
    val query = colors.filter(_.id === color.id).update(color)
    exec(query)
    color
  }

  def delete(id: Int): Int = {
    val query = colors.filter(_.id === id).delete
    exec(query)
  }
}
