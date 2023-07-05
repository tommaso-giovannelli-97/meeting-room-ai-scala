package dtos

import entities.Room

import java.sql.Timestamp
import java.time.LocalDateTime

case class RoomDTO(colorId: Int, name: String, seats: Int) {
  def toEntity(): Room = {
    Room(None, this.colorId, this.name, this.seats, Some(Timestamp.valueOf(LocalDateTime.now())), Some(true))
  }
}

