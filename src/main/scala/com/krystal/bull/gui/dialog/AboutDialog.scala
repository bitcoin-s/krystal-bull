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

    val txt =
      s"""Krystal Bull v$version
         |
         |Krystal Bull is a full featured DLC Oracle.
         |This project is built on top of the Bitcoin-S DLC Oracle module.
         |
         |Repo: https://github.com/bitcoin-s/krystal-bull
         |""".stripMargin

    dialog.dialogPane().content = new TextArea {
      text = txt
      editable = false
    }

    val _ = dialog.showAndWait()
  }
}
