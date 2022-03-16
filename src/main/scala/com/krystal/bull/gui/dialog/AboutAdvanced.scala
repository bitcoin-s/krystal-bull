package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.{GlobalData}
import scalafx.Includes._
import scalafx.scene.control.{ButtonType, Dialog, TextArea}
import scalafx.stage.Window

object AdvancedDialog {

  def showAndWait(parentWindow: Window): Unit = {
    val dialog = new Dialog[Unit]() {
      initOwner(parentWindow)
      title = "Advanced Mode"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    val txt =
      s"""
         |Warning: You have entered Advanced Mode. 
         |
         |Proceed with caution.
         |""".stripMargin

    dialog.dialogPane().content = new TextArea {
      text = txt
      editable = false
    }

    val _ = dialog.showAndWait()
  }
}
