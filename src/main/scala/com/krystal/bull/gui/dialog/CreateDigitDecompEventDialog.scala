package com.krystal.bull.gui.dialog

import java.time.{Instant, LocalTime, ZoneOffset}

import com.krystal.bull.gui.GlobalData
import com.krystal.bull.gui.home.InitEventParams
import org.bitcoins.core.number._
import org.bitcoins.core.protocol.tlv.DigitDecompositionEventDescriptorV0TLV
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.stage.Window

object CreateDigitDecompEventDialog {

  def showAndWait(parentWindow: Window): Option[InitEventParams] = {
    val dialog = new Dialog[Option[InitEventParams]]() {
      initOwner(parentWindow)
      title = "Create Digit Decomposition Event"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets
    dialog.resizable = true

    val eventNameTF = new TextField()
    val datePicker = new DatePicker()
    val maxTF = new TextField()
    maxTF.text.addListener {
      (
          _: javafx.beans.value.ObservableValue[_ <: String],
          _: String,
          newVal: String) =>
        if (!newVal.matches("\\d*"))
          maxTF.setText(newVal.replaceAll("[^\\d]", ""))
    }

    val isSignedCheckBox = new CheckBox() {
      alignmentInParent = Pos.Center
    }

    val unitTF = new TextField()
    val precisionTF = new TextField() {
      text = "0"
    }
    precisionTF.text.addListener {
      (
          _: javafx.beans.value.ObservableValue[_ <: String],
          _: String,
          newVal: String) =>
        if (!newVal.matches("\\d*"))
          precisionTF.setText(newVal.replaceAll("[^\\d]", ""))
    }

    dialog.dialogPane().content = new GridPane {
      padding = Insets(top = 10, right = 10, bottom = 10, left = 10)
      hgap = 10
      vgap = 10

      var row = 0
      add(new Label("Event Name"), 0, row)
      add(eventNameTF, 1, row)

      row += 1
      add(new Label("Maturity Date"), 0, row)
      add(datePicker, 1, row)

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
        val eventName = eventNameTF.text.value

        val maturityDateEpoch =
          datePicker.value.value.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC)

        val maturityDate = Instant.ofEpochSecond(maturityDateEpoch)

        val maxNumber = maxTF.text.value.toLong

        // log 2 of maxNumber gives us how many base 2 digits are needed
        val digitsNeeded =
          Math.ceil(Math.log10(maxNumber) / Math.log10(2)).toInt

        val isSigned = isSignedCheckBox.selected.value
        val unit = unitTF.text.value
        val precision = precisionTF.text.value.toLong

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
