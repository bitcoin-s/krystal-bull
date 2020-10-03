package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.GlobalData
import scalafx.Includes._
import scalafx.scene.control.{ButtonType, Dialog, TextArea}
import scalafx.scene.image.{Image, ImageView}
import scalafx.stage.Window

object AboutDialog {

  def showAndWait(parentWindow: Window): Unit = {
    val dialog = new Dialog[Unit]() {
      initOwner(parentWindow)
      title = "About"
      graphic = new ImageView(new Image("/icons/krystal_bull.png")) {
        fitHeight = 100
        fitWidth = 100
      }
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    dialog.dialogPane().content = new TextArea {
      text =
        "Krystal Bull v0.1\n\nCreated by: benthecarman\n\nRepo: https://github.com/benthecarman/krystal-bull"
      editable = false
    }

    val _ = dialog.showAndWait()
  }
}
