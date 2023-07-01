package repositories

import com.typesafe.config.ConfigFactory
import entities.{Account, Accounts}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object AccountRepository {

  val db: Database = {
    val dataSource = new org.postgresql.ds.PGSimpleDataSource()
    dataSource.setUser("postgres")
    dataSource.setPassword("postgres")
    dataSource.setURL("jdbc:postgresql://localhost:5432/meeting-room")

    Database.forDataSource(dataSource, None)
  }

  val accounts = TableQuery[Accounts]

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2.seconds)

  def create(account: Account): Account = {
    val query = accounts += account
    exec(query)
    account
  }

  def getById(id: String): Option[Account] = {
    val query = accounts.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(): Seq[Account] = {
    exec(accounts.result)
  }

  def update(account: Account): Account = {
    val query = accounts.filter(_.id === account.id).update(account)
    exec(query)
    account
  }

  def delete(id: String): Int = {
    val query = accounts.filter(_.id === id).delete
    exec(query)
  }
}
