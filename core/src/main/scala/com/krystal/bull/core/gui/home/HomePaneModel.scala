package com.krystal.bull.core.gui.home

import com.krystal.bull.core.Event
import com.krystal.bull.core.gui.TaskRunner
import com.krystal.bull.core.gui.dialog._
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

class HomePaneModel() {
  var taskRunner: TaskRunner = _

  // Sadly, it is a Java "pattern" to pass null into
  // constructors to signal that you want some default
  val parentWindow: ObjectProperty[Window] = {
    ObjectProperty[Window](null.asInstanceOf[Window])
  }

  def createEvent(): Option[InitEventParams] = {
    CreateEventDialog.showAndWait(parentWindow.value)
  }

  def viewEvent(event: Event): Unit = {
    ViewEventDialog.showAndWait(parentWindow.value, event)
  }
}

case class InitEventParams(label: String, outcomes: Vector[String])
