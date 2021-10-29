package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.GUIUtil.numberFormatter
import com.krystal.bull.gui.home.InitEventParams
import com.krystal.bull.gui.{GUIUtil, GlobalData, KrystalBullUtil}
import org.bitcoins.core.number._
import org.bitcoins.core.protocol.tlv.DigitDecompositionEventDescriptorV0TLV
import org.bitcoins.core.util.TimeUtil
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.stage.Window
import scalafx.util.StringConverter

import java.util.Date

object CreateNumericEventDialog {

  def showAndWait(
      parentWindow: Window,
      initParamsOpt: Option[InitEventParams]): Option[InitEventParams] = {
    val descriptorOpt =
      initParamsOpt.map(
        _.descriptorTLV.asInstanceOf[DigitDecompositionEventDescriptorV0TLV])

    val titleStr = initParamsOpt match {
      case Some(value) => s"Clone of ${value.sanitizedEventName}"
      case None        => "Create New Numeric Event"
    }

    val dialog = new Dialog[Option[InitEventParams]]() {
      initOwner(parentWindow)
      title = titleStr
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets
    dialog.resizable = true

    val eventNameTF = new TextField() {
      text = initParamsOpt.map(_.sanitizedEventName).getOrElse("")
      minWidth = 300
    }

    val appendDateCheckBox = new CheckBox() {
      alignmentInParent = Pos.CenterRight
      selected = initParamsOpt.exists(_.hasAppendedDate)
    }

    val datePicker: DatePicker = new DatePicker() {
      minWidth = 300
    }

    val hourPicker = new ComboBox[Int](1.to(12)) {
      value = initParamsOpt.map(_.hour).getOrElse(12)
    }
    val minutePicker = new ComboBox[Int](0.to(59)) {
      value = initParamsOpt.map(_.minute).getOrElse(0)
      converter = new StringConverter[Int] {
        override def fromString(string: String): Int = string.toInt

        override def toString(t: Int): String = {
          if (t < 10) s"0$t"
          else t.toString
        }
      }
    }

    val amOrPmPicker = new ComboBox[String](Vector("AM", "PM")) {
      value = initParamsOpt.map(_.isAM) match {
        case Some(true) | None => "AM"
        case Some(false)       => "PM"
      }
    }

    val maxTF = new TextField() {
      text = descriptorOpt.map(_.maxNum.toString()).getOrElse("")
    }
    GUIUtil.setNumericInput(maxTF)

    val isSignedCheckBox = new CheckBox() {
      alignmentInParent = Pos.Center
      selected = descriptorOpt.exists(_.minNum != 0)
    }

    val unitTF = new TextField() {
      text = descriptorOpt.map(_.unit.normStr).getOrElse("")
    }

    val precisionTF = new TextField() {
      text = descriptorOpt.map(_.precision.toLong.toString).getOrElse("0")
    }

    GUIUtil.setNumericInput(precisionTF)

    dialog.dialogPane().content = new GridPane {
      padding = Insets(top = 10, right = 10, bottom = 10, left = 10)
      hgap = 10
      vgap = 10

      var row = 0
      add(new Label("Event Name"), 0, row)
      add(eventNameTF, 1, row)

      if (GlobalData.advancedMode) {
        row += 1
        add(new Label("Append date to name"), 0, row)
        add(appendDateCheckBox, 1, row)
      }

      row += 1
      add(new Label("Maturity Date"), 0, row)
      add(datePicker, 1, row)

      if (GlobalData.advancedMode) {
        row += 1
        add(new Label("Maturity Time"), 0, row)
        val hbox = new HBox() {
          alignment = Pos.Center
          spacing = 10
          children =
            Vector(hourPicker, minutePicker, amOrPmPicker, new Label("UTC"))
        }
        add(hbox, 1, row)
      }

      row += 1
      add(new Label("Max Outcome"), 0, row)
      add(maxTF, 1, row)

      row += 1
      add(new Label("Include Negatives"), 0, row)
      add(isSignedCheckBox, 1, row)

      row += 1
      add(new Label("Units"), 0, row)
      add(unitTF, 1, row)

      row += 1
      add(new Label("Precision"), 0, row)
      add(precisionTF, 1, row)
    }

    // Enable/Disable OK button depending on whether all data was entered.
    val okButton = dialog.dialogPane().lookupButton(ButtonType.OK)
    // Simple validation that sufficient data was entered
    okButton.disable <== eventNameTF.text.isEmpty

    // When the OK button is clicked, convert the result to a T.
    dialog.resultConverter = dialogButton =>
      if (dialogButton == ButtonType.OK) {

        val maturityDate = KrystalBullUtil.toInstant(
          datePicker = datePicker,
          hourPicker = hourPicker,
          minutePicker = minutePicker,
          amOrPmPicker = amOrPmPicker)

        val eventName = {
          val name = eventNameTF.text.value
          if (appendDateCheckBox.selected.value) {
            val timeStr = TimeUtil.iso8601ToString(Date.from(maturityDate))
            s"$name $timeStr"
          } else name
        }

        val maxNumber = numberFormatter.parse(maxTF.text.value).longValue()

        // log 2 of maxNumber gives us how many base 2 digits are needed
        val digitsNeeded =
          Math.ceil(Math.log(maxNumber.toDouble) / Math.log(2)).toInt

        val isSigned = isSignedCheckBox.selected.value
        val unit = unitTF.text.value
        val precision =
          numberFormatter.parse(precisionTF.text.value).longValue()

        val descriptor = DigitDecompositionEventDescriptorV0TLV(
          base = UInt16(2),
          isSigned = isSigned,
          numDigits = digitsNeeded,
          unit = unit,
          precision = Int32(precision))

        val params = InitEventParams(eventName, maturityDate, descriptor)

        Some(params)
      } else None

    dialog.showAndWait() match {
      case Some(Some(params: InitEventParams)) =>
        Some(params)
      case Some(_) | None => None
    }
  }
}
