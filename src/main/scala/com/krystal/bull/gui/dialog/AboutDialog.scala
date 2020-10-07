package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.GlobalData
import scalafx.Includes._
import scalafx.scene.control.{ButtonType, Dialog, TextArea}
import scalafx.stage.Window

object AboutDialog {

  def showAndWait(parentWindow: Window): Unit = {
    val dialog = new Dialog[Unit]() {
      initOwner(parentWindow)
      title = "About"
      graphic = GlobalData.logo
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
