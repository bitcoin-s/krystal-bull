package com.krystal.bull.core.gui

import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import com.krystal.bull.core.gui.settings.Themes
import javafx.scene.paint.Color
import org.bitcoins.core.config._
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.dlc.oracle._
import scalafx.beans.property.{ObjectProperty, StringProperty}

import scala.concurrent.ExecutionContextExecutor
import scala.util.Properties

object GlobalData {

  implicit val system: ActorSystem = ActorSystem("krystal-bull")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val DEFAULT_DATADIR: Path =
    Paths.get(Properties.userHome, ".krystal-bull")

  implicit val appConfig: DLCOracleAppConfig = DLCOracleAppConfig(
    DEFAULT_DATADIR)

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

  var oracle: DLCOracle = _

  def stakingAddress: BitcoinAddress = oracle.stakingAddress(network)

  val stakedAmountText: StringProperty = StringProperty("Fetching balance...")
}
