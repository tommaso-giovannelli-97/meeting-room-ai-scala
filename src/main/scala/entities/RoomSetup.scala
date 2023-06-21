package entities

import slick.jdbc.PostgresProfile.api._

case class RoomSetup(id: Int, roomId: Int, equipmentId: Int, isActive: Option[Boolean]) extends Product with Serializable

class RoomSetups(tag: Tag) extends Table[RoomSetup](tag, "room_setup") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def roomId = column[Int]("room_id")

  def equipmentId = column[Int]("equipment_id")

  def isActive = column[Option[Boolean]]("is_active")

  def * = (id, roomId, equipmentId, isActive).mapTo[RoomSetup]

  def fkEquipmentId = foreignKey("room_setup_equipment_id_fkey", equipmentId, TableQuery[Equipments])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)

  def fkRoomId = foreignKey("room_setup_room_id_fkey", roomId, TableQuery[Rooms])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
}
