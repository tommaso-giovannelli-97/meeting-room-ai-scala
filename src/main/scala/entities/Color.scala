package entities

import slick.jdbc.PostgresProfile.api._

case class Color(id: Option[Int], name: String) extends Product with Serializable

class Colors(tag: Tag) extends Table[Color](tag, "color") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def * = (id, name).mapTo[Color]

  def pk = primaryKey("color_pkey", id)
}
