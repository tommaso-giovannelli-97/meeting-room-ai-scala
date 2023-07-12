package repositories

import dtos.EquipmentDTO
import entities.{Equipment, Equipments}
import exceptions.NotFoundException
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
    val query = (equipments returning equipments) += equipment
    exec(query)
  }

  def getById(id: Int): Option[Equipment] = {
    val query = equipments.filter(_.id === id)
    exec(query.result.headOption)
  }

  def getAll(): Seq[Equipment] = {
    exec(equipments.result)
  }

  def update(id: Int, equipment: Equipment): Equipment = {
    val equipmentToUpdate: Option[Equipment] = EquipmentRepository.getById(id)
    equipmentToUpdate match {
      case None => throw new NotFoundException("Equipment with given id doesn't exist")
      case Some(_) =>
        val query = equipments.filter(_.id === id).update(equipment)
        exec(query)
        val updatedEquipment: Option[Equipment] = EquipmentRepository.getById(id)
        updatedEquipment.getOrElse(throw new NotFoundException("Equipment with given id doesn't exist"))
    }
  }

  def delete(id: Int): Int = {
    val equipmentToDelete: Option[Equipment] = EquipmentRepository.getById(id)
    equipmentToDelete match {
      case None => throw new NotFoundException("Equipment with given id doesn't exist")
      case Some(_) =>
        val query = equipments.filter(_.id === id).delete
        exec(query)
    }
  }
}
