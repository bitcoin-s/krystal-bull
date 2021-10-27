package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.GlobalData
import com.krystal.bull.gui.config.KrystalBullAppConfig
import org.bitcoins.core.crypto.MnemonicCode
import org.bitcoins.crypto.AesPassword
import org.bitcoins.dlc.oracle.config.DLCOracleAppConfig
import org.bitcoins.keymanager.{DecryptedMnemonic, WalletStorage}
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, VBox}
import scalafx.stage.Window

import java.nio.file.Files
import java.time.Instant
import scala.concurrent.ExecutionContext

object InitOracleDialog {

  def showAndWait(parentWindow: Window)(implicit
      ec: ExecutionContext): Option[DLCOracleAppConfig] = {
    val dialog = new Dialog[Option[DLCOracleAppConfig]]() {
      initOwner(parentWindow)
      title = "Initialize Oracle"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    val oracleNameTF = new TextField()
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

      add(new Label("Oracle Name"), 0, 0)
      add(oracleNameTF, 1, 0)
      add(new Label("Password"), 0, 1)
      add(passwordTF, 1, 1)
    }

    dialog.dialogPane().content = new VBox {
      children = Vector(helpText, seedGrid, passFields)
    }

    // When the OK button is clicked, convert the result to a T.
    dialog.resultConverter = dialogButton =>
      if (dialogButton == ButtonType.OK) {
        val oracleName = oracleNameTF.text.value
        Files.write(GlobalData.oracleNameFile, oracleName.getBytes("UTF-8"))

        val oracleAppConfig = writeInputSeedToFile(passwordTF, mnemonicCode)
        Some(oracleAppConfig)
      } else None

    dialog.showAndWait() match {
      case Some(Some(appConfig: DLCOracleAppConfig)) =>
        Some(appConfig)
      case Some(_) | None => None
    }
  }

  /** Writes the given mnemonic code to the default datadir/seeds/ */
  def writeInputSeedToFile(
      passwordTF: PasswordField,
      mnemonicCode: MnemonicCode)(implicit
      ec: ExecutionContext): DLCOracleAppConfig = {
    val password = passwordTF.text.value
    val aesPass = AesPassword.fromStringOpt(password)
    GlobalData.setPassword(aesPass)

    //write mnemonic to disk
    val _ = WalletStorage.writeSeedToDisk(
      GlobalData.seedPath,
      DecryptedMnemonic(mnemonicCode, Instant.now))
    val oracleAppConfig =
      DLCOracleAppConfig.fromDatadir(KrystalBullAppConfig.DEFAULT_DATADIR)
    oracleAppConfig
  }
}
