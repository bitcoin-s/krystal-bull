package com.krystal.bull.core.gui.landing

import com.krystal.bull.core.gui.dialog.InitOracleDialog
import com.krystal.bull.core.gui.{GUI, GlobalData, TaskRunner}
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
            GlobalData.appConfig.initialize(kb)
          case None =>
            ()
        }
      }
    )

    if (krystalBullOpt.isDefined) {
      GUI.changeToHomeScene()
    }
  }
}
