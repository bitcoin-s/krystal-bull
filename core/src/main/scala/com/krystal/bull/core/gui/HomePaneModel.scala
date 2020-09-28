package com.krystal.bull.core.gui

import com.krystal.bull.core.gui.GlobalData._
import com.krystal.bull.core.gui.dialog._
import com.krystal.bull.core.storage.SeedStorage
import com.krystal.bull.core.{Event, KrystalBull}
import org.bitcoins.core.util.FutureUtil
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
    krystalBullOpt match {
      case None =>
        val aesPasswordOpt = UnlockDialog.showAndWait(parentWindow.value)

        taskRunner.run(
          caption = "Set Oracle",
          op = {
            krystalBullOpt match {
              case None =>
                aesPasswordOpt match {
                  case Some(password) =>
                    val extKey =
                      SeedStorage.getPrivateKeyFromDisk(appConfig.seedPath,
                                                        password,
                                                        None)
                    val kb = KrystalBull(extKey)
                    appConfig.initialize(kb).map { _ =>
                      krystalBullOpt = Some(kb)
                    }
                  case None =>
                    FutureUtil.unit
                }
              case Some(_) =>
                FutureUtil.unit
            }
          }
        )
      case Some(_) =>
        ()
    }
  }

  def createEvent(): Option[InitEventParams] = {
    CreateEventDialog.showAndWait(parentWindow.value)
  }

  def viewEvent(event: Event): Unit = {
    ViewEventDialog.showAndWait(parentWindow.value, event)
  }
}

case class InitEventParams(label: String, outcomes: Vector[String])
