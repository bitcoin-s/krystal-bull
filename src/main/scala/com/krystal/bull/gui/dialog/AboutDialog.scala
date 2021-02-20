package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.{GlobalData, KrystalBullUtil}
import scalafx.Includes._
import scalafx.scene.control.{ButtonType, Dialog, TextArea}
import scalafx.stage.Window

object AboutDialog {

  def showAndWait(parentWindow: Window): Unit = {
    val dialog = new Dialog[Unit]() {
      initOwner(parentWindow)
      title = "About"
      graphic = KrystalBullUtil.logo()
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    val version: String = getClass.getPackage.getImplementationVersion

    dialog.dialogPane().content = new TextArea {
      text =
        s"Krystal Bull v$version\n\nCreated by: benthecarman\n\nRepo: https://github.com/benthecarman/krystal-bull"
      editable = false
    }

    val _ = dialog.showAndWait()
  }
}
