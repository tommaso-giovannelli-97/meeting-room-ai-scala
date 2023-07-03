package entities

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp
import java.time.LocalDateTime

case class Equipment(id: Option[Int], name: String, createdAt: Option[Timestamp], isActive: Option[Boolean]) extends Product with Serializable

class Equipments(tag: Tag) extends Table[Equipment](tag, "equipment") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def createdAt = column[Option[Timestamp]]("created_at", O.Default(Some(Timestamp.valueOf(LocalDateTime.now()))))
  def isActive = column[Option[Boolean]]("is_active", O.Default(Some(true)))

  def * = (id, name, createdAt, isActive).mapTo[Equipment]
}