package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.GlobalData
import org.bitcoins.core.crypto.MnemonicCode
import org.bitcoins.dlc.oracle.config.DLCOracleAppConfig
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, VBox}
import scalafx.stage.Window

object RestoreOracleDialog {

  def showAndWait(parentWindow: Window): Option[DLCOracleAppConfig] = {
    val dialog = new Dialog[Option[DLCOracleAppConfig]]() {
      initOwner(parentWindow)
      title = "Restore Oracle"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    val passwordTF = new PasswordField()

    val helpText = new Label("Enter your recovery words and password")
    val wordTFs = 1.to(24).map(_ => new TextField())

    val seedGrid: GridPane = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      0.to(23).map { index =>
        val tf = wordTFs(index)
        if (index % 2 == 0) {
          add(new Label(s"${index + 1})"), 0, index / 2)
          add(tf, 1, index / 2)
        } else {
          add(new Label(s"${index + 1})"), 2, index / 2)
          add(tf, 3, index / 2)
        }
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

    // When the OK button is clicked, convert the result to a T.
    dialog.resultConverter = dialogButton =>
      if (dialogButton == ButtonType.OK) {
        val words = wordTFs.map(_.text.value)
        val mnemonicCode = MnemonicCode.fromWords(words.toVector)
        val oracleAppConfig =
          InitOracleDialog.writeInputSeedToFile(passwordTF, mnemonicCode)
        Some(oracleAppConfig)
      } else None

    dialog.dialogPane().getScene.getWindow.sizeToScene()

    dialog.showAndWait() match {
      case Some(Some(oracle: DLCOracleAppConfig)) =>
        Some(oracle)
      case Some(_) | None => None
    }
  }
}
