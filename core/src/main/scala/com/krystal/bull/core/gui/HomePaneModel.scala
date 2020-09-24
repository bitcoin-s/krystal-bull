package com.krystal.bull.core.gui

import com.krystal.bull.core.KrystalBull
import com.krystal.bull.core.gui.dialog.UnlockDialog
import com.krystal.bull.core.storage.SeedStorage
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

class HomePaneModel() {
  var taskRunner: TaskRunner = _

  // Sadly, it is a Java "pattern" to pass null into
  // constructors to signal that you want some default
  val parentWindow: ObjectProperty[Window] = {
    ObjectProperty[Window](null.asInstanceOf[Window])
  }

  def setOracle(): Unit = {
    GlobalData.krystalBullOpt match {
      case None =>
        val passwordOpt = UnlockDialog.showAndWait(parentWindow.value)

        taskRunner.run(
          caption = "Set Oracle",
          op = {
            passwordOpt match {
              case Some(password) =>
                val extKey = SeedStorage.getPrivateKeyFromDisk(
                  GlobalData.appConfig.seedPath,
                  password,
                  None)
                val kb = KrystalBull(extKey)(GlobalData.appConfig)
                GlobalData.appConfig.initialize(kb)
                GlobalData.krystalBullOpt = Some(kb)
              case None =>
            }
          }
        )
      case Some(_) =>
        ()
    }
  }
}
