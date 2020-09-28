package com.krystal.bull.core.gui.landing

import com.krystal.bull.core.KrystalBull
import com.krystal.bull.core.gui.GlobalData._
import com.krystal.bull.core.gui.dialog.InitOracleDialog
import com.krystal.bull.core.gui.{GUI, GlobalData, TaskRunner}
import com.krystal.bull.core.storage.SeedStorage
import org.bitcoins.core.util.FutureUtil
import org.bitcoins.crypto.AesPassword
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
            GlobalData.krystalBullOpt = Some(kb)
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

  def setOracle(password: AesPassword): Unit = {
    krystalBullOpt match {
      case None =>
        taskRunner.run(
          caption = "Set Oracle",
          op = {
            krystalBullOpt match {
              case None =>
                val extKey =
                  SeedStorage.getPrivateKeyFromDisk(appConfig.seedPath,
                                                    password,
                                                    None)
                val kb = KrystalBull(extKey)
                krystalBullOpt = Some(kb)
                appConfig.initialize(kb)
              case Some(_) =>
                FutureUtil.unit
            }
          }
        )
      case Some(_) =>
        ()
    }

    Thread.sleep(1000)
    if (krystalBullOpt.isDefined) {
      GUI.changeToHomeScene()
    }
  }
}
