package com.krystal.bull.gui.settings

import com.krystal.bull.gui.GUI.stage
import com.krystal.bull.gui.GlobalData
import javafx.scene.paint.Color

sealed abstract class Theme {
  def fileLocation: String

  def applyTheme: Boolean = {
    GlobalData.textColor.value = Color.WHITE
    stage.scene.value.getStylesheets.add(fileLocation)
  }

  def undoTheme: Boolean = {
    GlobalData.textColor.value = Color.BLACK
    stage.scene.value.getStylesheets.removeAll(fileLocation)
  }
}

object Themes {

  final case object DarkTheme extends Theme {
    override def fileLocation: String = "/themes/dark-theme.css"
  }

  val all: Vector[Theme] = Vector(DarkTheme)

  def fromString(str: String): Option[Theme] = {
    all.find(theme => str.toLowerCase() == theme.toString.toLowerCase)
  }
}
