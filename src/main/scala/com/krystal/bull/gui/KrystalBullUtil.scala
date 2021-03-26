package com.krystal.bull.gui

import org.bitcoins.core.util.TimeUtil
import scalafx.geometry.Pos
import scalafx.scene.control.{ComboBox, DatePicker}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane

import java.time.{Instant, ZoneOffset}

object KrystalBullUtil {

  /** Converts a date picker into a Long that represents seconds
    * since the epoch
    */
  def toInstant(
      datePicker: DatePicker,
      hourPicker: ComboBox[Int],
      minutePicker: ComboBox[Int],
      amOrPmPicker: ComboBox[String]): Instant = {
    //https://stackoverflow.com/a/23886207/967713
    val date: java.time.LocalDate = datePicker.delegate.getValue
    val hour = hourPicker.getValue
    val minute = minutePicker.getValue
    val isAm = amOrPmPicker.getValue == "AM"

    val hourMilitary = {
      val h = if (hour == 12) 0 else hour
      if (isAm) h else h + 12
    }

    val res = date.atTime(hourMilitary, minute).atZone(ZoneOffset.UTC).toInstant

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

  def logo(size: Int = 100, pos: Pos = Pos.CenterLeft): StackPane = {
    val laserEye = new ImageView(
      new Image("/icons/krystal_bull_laser_eyes.png")) {
      fitHeight = size
      fitWidth = size
      visible = false
      onMouseClicked = _ => visible = !visible.value
    }
    val standard = new ImageView(new Image("/icons/krystal_bull.png")) {
      fitHeight = size
      fitWidth = size
      onMouseClicked = _ => laserEye.visible = !laserEye.visible.value
    }
    new StackPane() {
      alignment = pos
      children = Vector(standard, laserEye)
    }
  }
}
