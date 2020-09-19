package com.krystal.bull

import java.nio.file.{Path, Paths}

import com.typesafe.config.Config
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

  override protected[bitcoins] def newConfigOfType(
      configOverrides: Seq[Config]): KrystalBullAppConfig =
    KrystalBullAppConfig(directory, configOverrides: _*)

  override protected[bitcoins] def moduleName: String = "config"

  override protected[bitcoins] def baseDatadir: Path = directory
}

object KrystalBullAppConfig {
  val DEFAULT_DATADIR: Path = Paths.get(Properties.userHome, ".krystal-bull")

  def fromDefaultDatadir(confs: Config*)(implicit
      ec: ExecutionContext): KrystalBullAppConfig =
    KrystalBullAppConfig(DEFAULT_DATADIR, confs: _*)
}
