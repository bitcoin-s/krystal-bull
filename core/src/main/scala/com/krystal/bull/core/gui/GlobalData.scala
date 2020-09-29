package com.krystal.bull.core.gui

import akka.actor.ActorSystem
import com.krystal.bull.core.gui.settings.Themes
import com.krystal.bull.core.{KrystalBull, KrystalBullAppConfig}
import org.bitcoins.core.config._
import org.bitcoins.core.protocol.BitcoinAddress
import scalafx.beans.property.{ObjectProperty, StringProperty}
import javafx.scene.paint.Color

import scala.concurrent.ExecutionContextExecutor

object GlobalData {

  implicit val system: ActorSystem = ActorSystem("krystal-bull")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  implicit val appConfig: KrystalBullAppConfig =
    KrystalBullAppConfig.fromDefaultDatadir()

  val statusText: StringProperty = StringProperty("")

  val textColor: ObjectProperty[Color] = ObjectProperty(Color.WHITE)

  var darkThemeEnabled: Boolean = true

  def currentStyleSheets: Seq[String] =
    if (GlobalData.darkThemeEnabled) {
      Seq(Themes.DarkTheme.fileLocation)
    } else {
      Seq.empty
    }

  var network: BitcoinNetwork = MainNet

  var krystalBull: KrystalBull = _

  def stakingAddress: BitcoinAddress = krystalBull.stakingAddress(network)

  val stakedAmountText: StringProperty = StringProperty("Fetching balance...")
}
