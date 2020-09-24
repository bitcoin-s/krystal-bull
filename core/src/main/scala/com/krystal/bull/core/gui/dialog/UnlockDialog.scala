package com.krystal.bull.core.gui.dialog

import com.krystal.bull.core.gui.GlobalData
import org.bitcoins.crypto.AesPassword
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.control.{ButtonType, Dialog, Label, PasswordField}
import scalafx.scene.layout.HBox
import scalafx.stage.Window

object UnlockDialog {

  def showAndWait(parentWindow: Window): Option[AesPassword] = {
    val dialog = new Dialog[Option[AesPassword]]() {
      initOwner(parentWindow)
      title = "Unlock"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    val passTF = new PasswordField()

    dialog.dialogPane().content = new HBox() {
      spacing = 10
      padding = Insets(20, 10, 10, 10)

      children = Vector(new Label("Password"), passTF)
    }

    // Enable/Disable OK button depending on whether all data was entered.
    val okButton = dialog.dialogPane().lookupButton(ButtonType.OK)
    // Simple validation that sufficient data was entered
    okButton.disable <== passTF.text.isEmpty

    // When the OK button is clicked, convert the result to a T.
    dialog.resultConverter = dialogButton =>
      if (dialogButton == ButtonType.OK) {
        val str = passTF.text.value

        AesPassword.fromStringOpt(str)
      } else None

    dialog.showAndWait() match {
      case Some(Some(pass: AesPassword)) =>
        Some(pass)
      case Some(_) | None =>
        None
    }
  }
}
