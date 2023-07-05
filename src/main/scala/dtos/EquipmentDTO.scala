package dtos

import entities.Equipment

import java.sql.Timestamp
import java.time.LocalDateTime

case class EquipmentDTO(name: String) {
  def toEntity(): Equipment = {
    Equipment(None, this.name, Some(Timestamp.valueOf(LocalDateTime.now())), Some(true))
  }
}
