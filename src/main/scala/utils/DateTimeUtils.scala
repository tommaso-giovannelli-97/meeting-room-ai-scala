package utils

import java.util.{Calendar, Date}

object DateTimeUtils {
  def getCurrentTimeStamp : Date  = {
    val d = Calendar.getInstance()
    d.getTime
  }
}
