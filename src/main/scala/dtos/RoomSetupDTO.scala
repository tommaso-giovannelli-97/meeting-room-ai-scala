package dtos

import entities.RoomSetup

import java.sql.Timestamp
import java.time.LocalDateTime

case class RoomSetupDTO(roomId: Int, equipmentId: Int){
  def toEntity(): RoomSetup = {
    RoomSetup(None, this.roomId, this.equipmentId, Some(true))
  }
}
