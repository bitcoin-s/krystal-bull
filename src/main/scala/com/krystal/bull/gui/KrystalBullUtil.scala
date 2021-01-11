package com.krystal.bull.gui

import org.bitcoins.core.util.TimeUtil
import scalafx.scene.control.DatePicker

import java.time.{Instant, ZoneOffset}

object KrystalBullUtil {

  /** Converts a date picker into a Long that represents seconds
    * since the epoch
    */
  def toInstant(datePicker: DatePicker): Instant = {
    //https://stackoverflow.com/a/23886207/967713
    val date: java.time.LocalDate = datePicker.delegate.getValue
    val res = date.atStartOfDay(ZoneOffset.UTC).toInstant

    if (!GlobalData.advancedMode) {
      // Validate that the event is not too far in the future
      val threeMon =
        TimeUtil.now
          .atZone(ZoneOffset.UTC)
          .toInstant
          .plusSeconds(2629800 * 3) // 2629800 seconds in a month

      require(
        res.isBefore(threeMon),
        "Date is too far in the future, use advanced mode to disable this check")
    }

    res
  }
}
