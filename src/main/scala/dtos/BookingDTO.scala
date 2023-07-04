package dtos

import entities.Booking

import java.sql.Timestamp
import java.time.LocalDateTime

case class BookingDTO(roomId: Int,
                      accountId: String,
                      fromTime: Timestamp,
                      untilTime: Timestamp,
                      meetingTitle: String,
                      notes: Option[String],
                      gMeetLink: Option[String]) {
  def toEntity(): Booking = {
    Booking(None, this.roomId, this.accountId, this.fromTime, this.untilTime, this.meetingTitle
    , this.notes, this.gMeetLink, Some(Timestamp.valueOf(LocalDateTime.now())), Some(true))
  }
}
