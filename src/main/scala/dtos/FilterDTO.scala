package dtos

case class FilterDTO(roomId: Option[Int], accountId: Option[String], fromTime: Option[String], untilTime: Option[String])
