package com.krystal.bull.gui

import scalafx.scene.control.DatePicker

import java.time.Instant

object KrystalBullUtil {

  /** Converts a date picker into a Long that represents seconds
    * since the epoch
    */
  def toInstant(datePicker: DatePicker): Instant = {
    val date: java.time.LocalDate = datePicker.delegate.getValue
    Instant.from(date)
  }
}
