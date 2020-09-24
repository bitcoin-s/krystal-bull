package com.krystal.bull.core.gui

import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

class HomePaneModel() {
  var taskRunner: TaskRunner = _

  // Sadly, it is a Java "pattern" to pass null into
  // constructors to signal that you want some default
  val parentWindow: ObjectProperty[Window] = {
    ObjectProperty[Window](null.asInstanceOf[Window])
  }

  def initOracle(): Unit = {

    taskRunner.run(
      caption = "Initialize Oracle",
      op = {}
    )
  }
}
