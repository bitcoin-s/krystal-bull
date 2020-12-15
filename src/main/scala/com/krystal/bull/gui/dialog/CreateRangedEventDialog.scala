package com.krystal.bull.gui.dialog

import java.time.{Instant, LocalTime, ZoneOffset}
import com.krystal.bull.gui.home.InitEventParams
import com.krystal.bull.gui.{GUIUtil, GlobalData, KrystalBullUtil}
import org.bitcoins.core.number._
import org.bitcoins.core.protocol.tlv.RangeEventDescriptorV0TLV
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.stage.Window

object CreateRangedEventDialog {

  def showAndWait(parentWindow: Window): Option[InitEventParams] = {
    val dialog = new Dialog[Option[InitEventParams]]() {
      initOwner(parentWindow)
      title = "Create Ranged Event"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets
    dialog.resizable = true

    val eventNameTF = new TextField()
    val datePicker = new DatePicker()

    val startTF = new TextField()
    GUIUtil.setNumericInput(startTF)

    val stepTF = new TextField()
    GUIUtil.setNumericInput(stepTF)

    val countTF = new TextField()
    GUIUtil.setNumericInput(countTF)

    val unitTF = new TextField()
    val precisionTF = new TextField()
    GUIUtil.setNumericInput(precisionTF)

    dialog.dialogPane().content = new GridPane {
      padding = Insets(top = 10, right = 10, bottom = 10, left = 10)
      hgap = 5
      vgap = 5

      var row = 0
      add(new Label("Event Name"), 0, row)
      add(eventNameTF, 1, row)

      row += 1
      add(new Label("Maturity Date"), 0, row)
      add(datePicker, 1, row)

      row += 1
      add(new Label("Start"), 0, row)
      add(startTF, 1, row)

      row += 1
      add(new Label("Step"), 0, row)
      add(stepTF, 1, row)

      row += 1
      add(new Label("Num Steps"), 0, row)
      add(countTF, 1, row)

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

        val maturityDate = KrystalBullUtil.toInstant(datePicker)

        val start = startTF.text.value.toLong
        val step = stepTF.text.value.toLong
        val count = countTF.text.value.toLong
        val unit = unitTF.text.value
        val precision = precisionTF.text.value.toLong

        val descriptor = RangeEventDescriptorV0TLV(start = Int32(start),
                                                   count = UInt32(count),
                                                   step = UInt16(step),
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
