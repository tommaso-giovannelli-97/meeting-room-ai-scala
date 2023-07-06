package utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
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
}
