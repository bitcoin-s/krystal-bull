package com.krystal.bull.core.gui.dialog

import com.krystal.bull.core.gui.GlobalData
import com.krystal.bull.core.gui.home.InitEventParams
import org.bitcoins.core.util.TimeUtil
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.stage.Window

object CreateEventDialog {

  def showAndWait(parentWindow: Window): Option[InitEventParams] = {
    val dialog = new Dialog[Option[InitEventParams]]() {
      initOwner(parentWindow)
      title = "Create Event"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets
    dialog.resizable = true

    val eventNameTF = new TextField()

    val outcomeMap: scala.collection.mutable.Map[Int, TextField] =
      scala.collection.mutable.Map.empty

    var nextOutcomeRow: Int = 2
    val keyGrid: GridPane = new GridPane {
      alignment = Pos.Center
      padding = Insets(top = 10, right = 10, bottom = 10, left = 10)
      hgap = 5
      vgap = 5
    }

    def addOutcomeRow(): Unit = {

      val outcomeTF = new TextField()
      val row = nextOutcomeRow
      outcomeMap.addOne((row, outcomeTF))

      keyGrid.add(new Label("Potential Outcome"), 0, row)
      keyGrid.add(outcomeTF, 1, row)

      nextOutcomeRow += 1
      dialog.dialogPane().getScene.getWindow.sizeToScene()
    }

    addOutcomeRow()
    addOutcomeRow()

    val addOutcomeButton: Button = new Button("+") {
      onAction = _ => addOutcomeRow()
    }

    dialog.dialogPane().content = new VBox() {
      padding = Insets(20, 10, 10, 10)
      spacing = 10
      alignment = Pos.Center

      val baseData: Node = new HBox() {
        spacing = 10
        alignment = Pos.Center
        children = Vector(new Label("Event Name"), eventNameTF)
      }

      val outcomes: Node = new VBox {
        alignment = Pos.Center

        val label: HBox = new HBox {
          alignment = Pos.Center
          spacing = 10
          children = Vector(new Label("Potential Outcomes"), addOutcomeButton)
        }
        children = Vector(label, keyGrid)
      }

      children = Vector(baseData, new Separator(), outcomes)
    }

    // Enable/Disable OK button depending on whether all data was entered.
    val okButton = dialog.dialogPane().lookupButton(ButtonType.OK)
    // Simple validation that sufficient data was entered
    okButton.disable <== eventNameTF.text.isEmpty

    // When the OK button is clicked, convert the result to a T.
    dialog.resultConverter = dialogButton =>
      if (dialogButton == ButtonType.OK) {
        val eventName = eventNameTF.text.value

        val outcomeStrs = outcomeMap.values.toVector.distinct
        val outcomes = outcomeStrs.flatMap { keyStr =>
          if (keyStr.text.value.nonEmpty) {
            Some(keyStr.text.value)
          } else {
            None
          }
        }

        val params = InitEventParams(eventName, TimeUtil.now, outcomes)

        Some(params)
      } else None

    dialog.showAndWait() match {
      case Some(Some(params: InitEventParams)) =>
        Some(params)
      case Some(_) | None => None
    }
  }
}
