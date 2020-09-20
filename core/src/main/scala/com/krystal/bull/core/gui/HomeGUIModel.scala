package com.krystal.bull.core.gui

import com.krystal.bull.core.gui.dialog.AboutDialog
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

class HomeGUIModel() {

  // Sadly, it is a Java "pattern" to pass null into
  // constructors to signal that you want some default
  val parentWindow: ObjectProperty[Window] =
    ObjectProperty[Window](null.asInstanceOf[Window])

  def onAbout(): Unit = {
    AboutDialog.showAndWait(parentWindow.value)
  }
}
