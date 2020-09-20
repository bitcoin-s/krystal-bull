package com.krystal.bull.core

import java.nio.file.{Files, Path, Paths}

import com.krystal.bull.core.storage.SeedStorage
import com.typesafe.config.Config
import org.bitcoins.core.config.NetworkParameters
import org.bitcoins.core.util.FutureUtil
import org.bitcoins.db.AppConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Properties

case class KrystalBullAppConfig(
    private val directory: Path,
    private val conf: Config*)(implicit val ec: ExecutionContext)
    extends AppConfig {
  override def start(): Future[Unit] = FutureUtil.unit

  override type ConfigType = KrystalBullAppConfig

  override def newConfigOfType(
      configOverrides: Seq[Config]): KrystalBullAppConfig =
    KrystalBullAppConfig(directory, configOverrides: _*)

  override def moduleName: String = "config"

  override def baseDatadir: Path = directory

  lazy val networkParameters: NetworkParameters = chain.network

  /** The path to our encrypted mnemonic seed */
  lazy val seedPath: Path = {
    baseDatadir.resolve(SeedStorage.ENCRYPTED_SEED_FILE_NAME)
  }

  /** Checks if our oracle as a mnemonic seed associated with it */
  def seedExists(): Boolean = {
    Files.exists(seedPath)
  }
}

object KrystalBullAppConfig {
  val DEFAULT_DATADIR: Path = Paths.get(Properties.userHome, ".krystal-bull")

  def fromDefaultDatadir(confs: Config*)(implicit
      ec: ExecutionContext): KrystalBullAppConfig =
    KrystalBullAppConfig(DEFAULT_DATADIR, confs: _*)
}
