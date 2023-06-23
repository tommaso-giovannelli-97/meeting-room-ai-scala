package repositories

import com.typesafe.config.ConfigFactory
import entities.{RoomSetup, RoomSetups}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object RoomSetupRepository {
  val config = ConfigFactory.load()
  val db = Database.forConfig("postgresConnection", config)

  val roomSetups = TableQuery[RoomSetups]

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2.seconds)

  def create(roomSetup: RoomSetup): RoomSetup = {
    val query = roomSetups += roomSetup
    exec(query)
    roomSetup
  }

  def getById(id: Int): Option[RoomSetup] = {
    val query = roomSetups.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(): Seq[RoomSetup] = {
    exec(roomSetups.result)
  }

  def update(roomSetup: RoomSetup): RoomSetup = {
    val query = roomSetups.filter(_.id === roomSetup.id).update(roomSetup)
    exec(query)
    roomSetup
  }

  def delete(id: Int): Int = {
    val query = roomSetups.filter(_.id === id).delete
    exec(query)
  }
}

