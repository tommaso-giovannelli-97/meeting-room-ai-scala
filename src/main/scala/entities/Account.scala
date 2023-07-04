package entities

import slick.jdbc.PostgresProfile.api._
case class Account(id: String, name: String, email: Option[String], isActive: Option[Boolean]) extends Product with Serializable

class Accounts(tag: Tag) extends Table[Account](tag, "account") {
  def * = (id, name, email, isActive).mapTo[Account]

  def id = column[String]("id", O.PrimaryKey)

  def name = column[String]("name")

  def isActive = column[Option[Boolean]]("is_active", O.Default(Some(true)))

  def emailKey = index("user_email_key", email, unique = true)

  def email = column[Option[String]]("email")
}
