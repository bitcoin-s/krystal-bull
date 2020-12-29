package com.krystal.bull.gui

import akka.actor.ActorSystem
import com.krystal.bull.gui.settings.Themes
import javafx.scene.paint.Color
import org.bitcoins.core.config._
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.dlc.oracle._
import org.bitcoins.dlc.oracle.config.DLCOracleAppConfig
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.scene.image.{Image, ImageView}

import java.nio.file.{Path, Paths}
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

  def currentStyleSheets: Seq[String] = {
    val loaded = if (GlobalData.darkThemeEnabled) {
      Seq(Themes.DarkTheme.fileLocation)
    } else {
      Seq.empty
    }

    "/themes/base.css" +: loaded
  }

  var network: BitcoinNetwork = MainNet

  var oracle: DLCOracle = _

  def stakingAddress: BitcoinAddress = oracle.stakingAddress(network)

  val stakedAmountText: StringProperty = StringProperty("Fetching balance...")

  def logo: ImageView =
    new ImageView(new Image("/icons/krystal_bull.png")) {
      fitHeight = 100
      fitWidth = 100
    }

}
