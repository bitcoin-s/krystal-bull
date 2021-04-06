package com.krystal.bull.gui.landing

import com.krystal.bull.gui.GlobalData._
import com.krystal.bull.gui.dialog._
import com.krystal.bull.gui.{GUI, GlobalData, TaskRunner}
import org.bitcoins.core.crypto.ExtKeyVersion.SegWitMainNetPriv
import org.bitcoins.crypto.AesPassword
import org.bitcoins.dlc.oracle.DLCOracle
import org.bitcoins.keymanager.WalletStorage
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

import scala.concurrent.Future
import scala.util.Try

class LandingPaneModel() {
  var taskRunner: TaskRunner = _

  // Sadly, it is a Java "pattern" to pass null into
  // constructors to signal that you want some default
  val parentWindow: ObjectProperty[Window] = {
    ObjectProperty[Window](null.asInstanceOf[Window])
  }

  def initOracle(): Unit = {
    val krystalBullOpt = InitOracleDialog.showAndWait(parentWindow.value)

    taskRunner.run(
      caption = "Initialize Oracle",
      op = {
        krystalBullOpt match {
          case Some(oracle) =>
            GlobalData.oracle = oracle
            oracle.conf.initialize()
          case None =>
            Future.unit
        }
      }
    )

    if (krystalBullOpt.isDefined) {
      Thread.sleep(1000)
      GUI.changeToHomeScene()
    }
  }

  def restoreOracle(): Unit = {
    val oracleOpt = RestoreOracleDialog.showAndWait(parentWindow.value)

    taskRunner.run(
      caption = "Restore Oracle",
      op = {
        oracleOpt match {
          case Some(oracle) =>
            GlobalData.oracle = oracle
            oracle.conf.initialize()
          case None =>
            Future.unit
        }
      }
    )

    if (oracleOpt.isDefined) {
      Thread.sleep(1000)
      GUI.changeToHomeScene()
    }
  }

  def loadOracle(passwordOpt: Option[AesPassword]): Unit = {
    taskRunner.run(
      caption = "Load Oracle",
      op = {
        GlobalData.setPassword(passwordOpt)
        val extKey =
          WalletStorage.getPrivateKeyFromDisk(oracleAppConfig.seedPath,
                                              SegWitMainNetPriv,
                                              passwordOpt,
                                              None)

        val oracle = new DLCOracle(extKey)
        GlobalData.oracle = oracle
        oracleAppConfig.initialize()
      }
    )

    Try {
      Thread.sleep(1000)
      GUI.changeToHomeScene()
    }
    ()
  }
}
