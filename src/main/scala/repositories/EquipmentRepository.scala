package repositories

import dtos.EquipmentDTO
import entities.{Equipment, Equipments}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object EquipmentRepository {
  val db: Database = {
    val dataSource = new org.postgresql.ds.PGSimpleDataSource()
    dataSource.setUser("postgres")
    dataSource.setPassword("postgres")
    dataSource.setURL("jdbc:postgresql://localhost:5432/meeting-room")

    Database.forDataSource(dataSource, None)
  }

  val equipments = TableQuery[Equipments]

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2.seconds)

  def create(equipmentDTO: EquipmentDTO): Equipment = {
    val equipment = equipmentDTO.toEntity()
    val query = equipments += equipment
    exec(query)
    equipment
  }

  def getById(id: Int): Option[Equipment] = {
    val query = equipments.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(): Seq[Equipment] = {
    exec(equipments.result)
  }

  def update(equipment: Equipment): Equipment = {
    val query = equipments.filter(_.id === equipment.id).update(equipment)
    exec(query)
    equipment
  }

  def delete(id: Int): Int = {
    val query = equipments.filter(_.id === id).delete
    exec(query)
  }
}
