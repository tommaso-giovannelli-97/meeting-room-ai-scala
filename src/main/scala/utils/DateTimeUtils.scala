package utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.{Calendar, Date}

object DateTimeUtils {

  def convertStringToTimestamp(dateString: String): Option[Timestamp] = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    try {
      val parsedDate = format.parse(dateString)
      Some(new Timestamp(parsedDate.getTime))
    } catch {
      case _: Exception => None
    }
  }

  def convertOptionalStringToTimestamp(optionDateString: Option[String]): Option[Timestamp] = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    optionDateString match {
      case None => None
      case Some(dateString) =>
        try {
          val parsedDate = format.parse(dateString)
          Some(new Timestamp(parsedDate.getTime))
        } catch {
          case _: Exception => None
        }
    }

  }

  def addDaysToTimestamp(timestamp: Timestamp, numberOfDays : Int) : Timestamp = {
    val calendar = Calendar.getInstance()
    calendar.setTime(timestamp)
    calendar.add(Calendar.DAY_OF_YEAR, numberOfDays)
    new Timestamp(calendar.getTimeInMillis)
  }

  def addMinutesToTimestamp(timestamp: Timestamp, numberOfMinutes: Int): Timestamp = {
    val calendar = Calendar.getInstance()
    calendar.setTime(timestamp)
    calendar.add(Calendar.MINUTE, numberOfMinutes)
    new Timestamp(calendar.getTimeInMillis)
  }

  def toBeginningOfDay(timestamp: Timestamp): Timestamp = {
    val cal = java.util.Calendar.getInstance()
    cal.setTime(timestamp)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    new Timestamp(cal.getTimeInMillis)
  }

  def toEndOfDay(timestamp: Timestamp): Timestamp = {
    val cal = java.util.Calendar.getInstance()
    cal.setTime(timestamp)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
    cal.set(java.util.Calendar.MINUTE, 59)
    cal.set(java.util.Calendar.SECOND, 59)
    cal.set(java.util.Calendar.MILLISECOND, 999)
    new Timestamp(cal.getTimeInMillis)
  }

  def toWorkDayBeginning(timestamp: Timestamp): Timestamp = {
    val cal = java.util.Calendar.getInstance()
    cal.setTime(timestamp)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 9)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    new Timestamp(cal.getTimeInMillis)
  }

  def toWorkDayEnd(timestamp: Timestamp): Timestamp = {
    val cal = java.util.Calendar.getInstance()
    cal.setTime(timestamp)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 18)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    new Timestamp(cal.getTimeInMillis)
  }

  def getDaysDifferencebetweenDates(startTimestamp: Timestamp, endTimestamp:Timestamp): Int = {
    val startDate = startTimestamp.toLocalDateTime.toLocalDate
    val endDate = endTimestamp.toLocalDateTime.toLocalDate
    ChronoUnit.DAYS.between(startDate, endDate).toInt
  }

  def getMinutesDifferenceBetweenDates(startTimestamp: Timestamp, endTimestamp: Timestamp): Int = {
    val startTime = startTimestamp.toLocalDateTime.toLocalTime
    val endTime = endTimestamp.toLocalDateTime.toLocalTime
    ChronoUnit.MINUTES.between(startTime, endTime).toInt
  }
}
