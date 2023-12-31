package repositories

import com.typesafe.config.ConfigFactory
import dtos.AccountDTO
import entities.{Account, Accounts}
import exceptions.NotFoundException
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

  def create(accountDTO: AccountDTO): Account = {
    val account = accountDTO.toEntity()
    //val query = accounts += account
    val query = (accounts returning accounts) += account
    exec(query)
  }

  def getById(id: String): Option[Account] = {
    val query = accounts.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(): Seq[Account] = {
    exec(accounts.result)
  }

  def update(account: Account): Account = {
    val accountToUpdate = AccountRepository.getById(account.id)
    accountToUpdate match {
      case None => throw new NotFoundException("Account with given id doesn't exist")
      case Some(_) =>
        val query = accounts.filter(_.id === account.id).update(account)
        exec(query)
        val updatedAccount: Option[Account] = AccountRepository.getById(account.id)
        updatedAccount.getOrElse(throw new NotFoundException("Account with given id doesn't exist"))
    }


  }

  def delete(id: String): Int = {
    val accountToDelete: Option[Account] = AccountRepository.getById(id)
    accountToDelete match {
      case None => throw new NotFoundException("Account with given id doesn't exist")
      case Some(_) =>
        val query = accounts.filter(_.id === id).delete
        exec(query)
    }
  }
}
