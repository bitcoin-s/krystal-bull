package com.krystal.bull.core.gui.dialog

import com.krystal.bull.core.KrystalBull
import com.krystal.bull.core.gui.GlobalData
import org.bitcoins.crypto.AesPassword
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, VBox}
import scalafx.stage.Window
import com.krystal.bull.core.gui.GlobalData.appConfig
import org.bitcoins.core.crypto.MnemonicCode

object InitOracleDialog {

  def showAndWait(parentWindow: Window): Option[KrystalBull] = {
    val dialog = new Dialog[Option[KrystalBull]]() {
      initOwner(parentWindow)
      title = "Initialize Oracle"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    val passwordTF = new PasswordField()

    val entropy = MnemonicCode.getEntropy256Bits
    val mnemonicCode = MnemonicCode.fromEntropy(entropy)

    val helpText = new Label(
      "Write down your recovery words and remember your password")

    val seedGrid: GridPane = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      var row: Int = 0
      var col: Int = 0
      def addWord(label: String): Unit = {
        add(new Label(label), col, row)
        col += 1
        if (col == 4) {
          col = 0
          row += 1
        }
      }
      mnemonicCode.zipWithIndex.foreach {
        case (word, index) =>
          val text = s"${index + 1}) $word"
          addWord(text)
      }
    }

    val passFields: GridPane = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      add(new Label("Password"), 0, 0)
      add(passwordTF, 1, 0)
    }

    dialog.dialogPane().content = new VBox {
      children = Vector(helpText, seedGrid, passFields)
    }

    // Enable/Disable OK button depending on whether all data was entered.
    val okButton = dialog.dialogPane().lookupButton(ButtonType.OK)
    // Simple validation that sufficient data was entered
    okButton.disable <== passwordTF.text.isEmpty

    // When the OK button is clicked, convert the result to a T.
    dialog.resultConverter = dialogButton =>
      if (dialogButton == ButtonType.OK) {
        val password = passwordTF.text.value
        val aesPass = AesPassword.fromString(password)

        val krystalBull =
          KrystalBull.fromMnemonicCode(mnemonicCode, aesPass, None)
        Some(krystalBull)
      } else None

    dialog.showAndWait() match {
      case Some(Some(kb: KrystalBull)) =>
        Some(kb)
      case Some(_) | None => None
    }
  }
}
