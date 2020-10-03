package com.krystal.bull.core.gui.landing

import com.krystal.bull.core.gui.GlobalData._
import com.krystal.bull.core.gui.dialog._
import com.krystal.bull.core.gui.{GUI, GlobalData, TaskRunner}
import org.bitcoins.core.crypto.ExtKeyVersion.SegWitMainNetPriv
import org.bitcoins.core.util.FutureUtil
import org.bitcoins.crypto.AesPassword
import org.bitcoins.dlc.oracle.DLCOracle
import org.bitcoins.keymanager.WalletStorage
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

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
          case Some(kb) =>
            GlobalData.oracle = kb
            appConfig.initialize(kb)
          case None =>
            FutureUtil.unit
        }
      }
    )

    if (krystalBullOpt.isDefined) {
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
            appConfig.initialize(oracle)
          case None =>
            FutureUtil.unit
        }
      }
    )

    if (oracleOpt.isDefined) {
      GUI.changeToHomeScene()
    }
  }

  def setOracle(password: AesPassword): Unit = {
    taskRunner.run(
      caption = "Set Oracle",
      op = {
        val extKey =
          WalletStorage.getPrivateKeyFromDisk(appConfig.seedPath,
                                              SegWitMainNetPriv,
                                              password,
                                              None)
        val oracle = DLCOracle(extKey)
        GlobalData.oracle = oracle
        appConfig.initialize(oracle)
      }
    )

    Thread.sleep(1000)
    GUI.changeToHomeScene()
  }
}
