package com.krystal.bull.gui

import scalafx.scene.control.DatePicker

import java.time.{Instant, ZoneId}

object KrystalBullUtil {

  /** Converts a date picker into a Long that represents seconds
    * since the epoch
    */
  def toInstant(datePicker: DatePicker): Instant = {
    //https://stackoverflow.com/a/23886207/967713
    val date: java.time.LocalDate = datePicker.delegate.getValue
    date.atStartOfDay(ZoneId.systemDefault()).toInstant()
  }
}
