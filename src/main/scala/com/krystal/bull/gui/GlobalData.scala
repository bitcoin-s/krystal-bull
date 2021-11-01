package com.krystal.bull.gui

import akka.actor.ActorSystem
import com.krystal.bull.gui.config.KrystalBullAppConfig
import com.krystal.bull.gui.config.KrystalBullAppConfig.DEFAULT_DATADIR
import com.krystal.bull.gui.settings.Themes
import com.typesafe.config.ConfigFactory
import javafx.scene.paint.Color
import org.bitcoins.commons.jsonmodels.ExplorerEnv
import org.bitcoins.core.config._
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.crypto.AesPassword
import org.bitcoins.dlc.oracle._
import org.bitcoins.dlc.oracle.config.DLCOracleAppConfig
import org.bitcoins.explorer.client.SbExplorerClient
import org.bitcoins.keymanager.WalletStorage
import scalafx.beans.property.{ObjectProperty, StringProperty}

import java.nio.file.Path
import scala.concurrent.ExecutionContextExecutor

object GlobalData {

  implicit val system: ActorSystem = ActorSystem("krystal-bull")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val seedPath = KrystalBullAppConfig.DEFAULT_DATADIR
    .resolve(WalletStorage.SEED_FOLDER_NAME)
    .resolve(WalletStorage.ENCRYPTED_SEED_FILE_NAME)

  val oracleNameFile: Path = DEFAULT_DATADIR.resolve("oracleName.txt")

  val config: KrystalBullAppConfig =
    KrystalBullAppConfig.fromDatadir(DEFAULT_DATADIR)

  private var oracleAppConfigOpt: Option[DLCOracleAppConfig] = None

  def getOracleAppConfig(
      aesPasswordOpt: Option[AesPassword]): DLCOracleAppConfig = {
    val appConfig: DLCOracleAppConfig = aesPasswordOpt match {
      case Some(pass) =>
        val overrideConf =
          ConfigFactory.parseString(
            s"bitcoin-s.keymanager.aesPassword = ${pass.toStringSensitive}")

        oracleAppConfigOpt match {
          case Some(oracleAppConfig) =>
            oracleAppConfig.newConfigOfType(Vector(overrideConf))
          case None =>
            DLCOracleAppConfig.fromDatadir(DEFAULT_DATADIR,
                                           Vector(overrideConf))
        }
      case None =>
        DLCOracleAppConfig.fromDatadir(DEFAULT_DATADIR)
    }
    oracleAppConfigOpt = Some(appConfig)

    appConfig
  }

  val statusText: StringProperty = StringProperty("")

  val textColor: ObjectProperty[Color] = ObjectProperty(Color.WHITE)

  var darkThemeEnabled: Boolean = config.darkMode

  var explorerEnv: ExplorerEnv = config.explorerEnv

  def currentStyleSheets: Seq[String] = {
    val loaded = if (GlobalData.darkThemeEnabled) {
      Seq(Themes.DarkTheme.fileLocation)
    } else {
      Seq.empty
    }

    "/themes/base.css" +: loaded
  }

  var oracle: DLCOracle = _

  var advancedMode: Boolean = config.advancedMode

  lazy val stakingAddress: BitcoinAddress = oracle.stakingAddress(MainNet)

  var stakedAmountTextOpt: Option[StringProperty] = None

  var oracleNameOpt: Option[String] = None

  def oracleExplorerClient: SbExplorerClient =
    SbExplorerClient(explorerEnv, None)
}
