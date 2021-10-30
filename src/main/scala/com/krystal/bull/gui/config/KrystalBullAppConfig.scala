package com.krystal.bull.gui.config

import com.krystal.bull.gui.GlobalData
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.bitcoins.commons.config.{AppConfig, ConfigOps}
import org.bitcoins.commons.jsonmodels.ExplorerEnv

import java.io.{File, IOException}
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Properties

case class KrystalBullAppConfig(
    private val directory: Path,
    private val confs: Config*)(implicit val ec: ExecutionContext)
    extends AppConfig {

  override def configOverrides: List[Config] = confs.toList

  override type ConfigType = KrystalBullAppConfig

  override def newConfigOfType(
      configOverrides: Seq[Config]): KrystalBullAppConfig =
    KrystalBullAppConfig(directory, configOverrides: _*)

  override def moduleName: String = KrystalBullAppConfig.moduleName

  override def baseDatadir: Path = directory

  override lazy val datadir: Path = baseDatadir

  override def start(): Future[Unit] = Future.unit

  override def stop(): Future[Unit] = Future.unit

  lazy val aesPasswordOpt: Option[String] = {
    config.getStringOrNone(s"$moduleName.aesPassword")
  }

  lazy val advancedMode: Boolean =
    config.getBooleanOpt(s"$moduleName.advancedMode").getOrElse(false)

  lazy val darkMode: Boolean =
    config.getBooleanOpt(s"$moduleName.darkMode").getOrElse(true)

  lazy val explorerEnv: ExplorerEnv = {
    config.getStringOrNone(s"$moduleName.explorer-env") match {
      case Some(value) => ExplorerEnv.fromString(value)
      case None        => ExplorerEnv.Production
    }
  }

  def writeToFile(): Path = {
    logger.info("Writing config to file")

    val conf = ConfigFactory
      .empty()
      .withValue(s"$moduleName.advancedMode",
                 ConfigValueFactory.fromAnyRef(GlobalData.advancedMode))
      .withValue(s"$moduleName.darkMode",
                 ConfigValueFactory.fromAnyRef(GlobalData.darkThemeEnabled))
      .withValue(s"$moduleName.explorer-env",
                 ConfigValueFactory.fromAnyRef(GlobalData.explorerEnv.toString))

    val str = conf
      .entrySet()
      .asScala
      .map { entry => s"${entry.getKey}: ${entry.getValue.render()}" }
      .mkString("\n")

    Files.write(KrystalBullAppConfig.file.toPath, str.getBytes)
  }
}

object KrystalBullAppConfig {

  private[bull] val DEFAULT_DATADIR: Path = {
    val path = if (Properties.isMac) {
      Paths.get(Properties.userHome,
                "Library",
                "Application Support",
                "Krystal Bull")
    } else if (Properties.isWin) {
      Paths.get("C:",
                "Users",
                Properties.userName,
                "Appdata",
                "Roaming",
                "KrystalBull")
    } else {
      Paths.get("/tmp", "krystal-bull")
    }

    val file = path.toFile
    if (!file.exists()) {
      if (!file.mkdirs()) {
        val ex = new IOException(s"Cannot create data directory `$path`")
        ex.printStackTrace()
        throw ex
      }
    }

    path
  }

  val file: File =
    KrystalBullAppConfig.DEFAULT_DATADIR.resolve("krystal-bull.conf").toFile

  val moduleName: String = "krystal-bull"

  def fromDatadir(datadir: Path)(implicit
      ec: ExecutionContext): KrystalBullAppConfig = {
    val conf = ConfigFactory.parseFile(file)
    KrystalBullAppConfig(datadir, conf)
  }
}
