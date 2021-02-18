package com.krystal.bull.gui

import scalafx.scene.control.{TextField, TextFormatter}

import scala.util.control.NonFatal
import scala.util.matching.Regex

object GUIUtil {

  val numericRegex: Regex = "-?([1-9,][0-9,]*)?".r

  def setNumericInput(textField: TextField): Unit = {
    textField.textFormatter =
      new TextFormatter[String]((c: TextFormatter.Change) => {
        if (!c.isContentChange) {
          c // no need for modification, if only the selection changes
        } else {
          val newText = c.getControlNewText
          if (
            newText.isEmpty || (!numericRegex.pattern.matcher(newText).matches)
          ) {
            c
          } else {
            val formatted = {
              try {
                val formatter = java.text.NumberFormat.getIntegerInstance
                val num = {
                  val numStr = newText.replaceAll("[,-]", "")
                  if (newText.startsWith("-"))
                    numStr.toLong * -1
                  else numStr.toLong
                }
                formatter.format(num)
              } catch {
                case NonFatal(_) => newText // allow input if error
              }
            }

            // replace with modified text
            c.setRange(0, c.getRangeEnd)
            c.setText(formatted)
            c.setCaretPosition(formatted.length)
            c.setAnchor(formatted.length)
            c
          }
        }
      })
  }
}
