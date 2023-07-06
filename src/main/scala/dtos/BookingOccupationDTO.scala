package dtos

import java.time.LocalDate

case class BookingOccupationDTO(date: LocalDate, from: String, to:String, isOccupied: Boolean)
