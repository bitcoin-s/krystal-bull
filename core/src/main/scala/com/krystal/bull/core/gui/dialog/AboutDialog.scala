package com.krystal.bull.core.gui.dialog

import com.krystal.bull.core.gui.GlobalData
import scalafx.Includes._
import scalafx.scene.control.{ButtonType, Dialog, TextArea}
import scalafx.stage.Window

object AboutDialog {

  def showAndWait(parentWindow: Window): Unit = {
    val dialog = new Dialog[Unit]() {
      initOwner(parentWindow)
      title = "About"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    dialog.dialogPane().content = new TextArea {
      text =
        "Krystal Bull v0.1\n\nCreated by: benthecarman\n\nRepo is: https://github.com/benthecarman/krystal-bull"
      editable = false
    }

    val _ = dialog.showAndWait()
  }
}
