package com.krystal.bull.core

import java.nio.file.{Files, Path, Paths}

import com.krystal.bull.core.storage.{EventDAO, EventOutcomeDAO, RValueDAO, SeedStorage}
import com.typesafe.config.Config
import org.bitcoins.core.config.NetworkParameters
import org.bitcoins.core.crypto.MnemonicCode
import org.bitcoins.core.util.{FutureUtil, TimeUtil}
import org.bitcoins.crypto.AesPassword
import org.bitcoins.db.{AppConfig, DbManagement, JdbcProfileComponent}
import org.bitcoins.keymanager.DecryptedMnemonic

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Properties

case class KrystalBullAppConfig(
    private val directory: Path,
    private val conf: Config*)(implicit val ec: ExecutionContext)
    extends AppConfig
    with DbManagement
    with JdbcProfileComponent[KrystalBullAppConfig] {

  import profile.api._

  override def start(): Future[Unit] = FutureUtil.unit

  override def appConfig: KrystalBullAppConfig = this

  override type ConfigType = KrystalBullAppConfig

  override def newConfigOfType(
      configOverrides: Seq[Config]): KrystalBullAppConfig =
    KrystalBullAppConfig(directory, configOverrides: _*)

  override def moduleName: String = "oracle"

  override def baseDatadir: Path = directory

  lazy val networkParameters: NetworkParameters = chain.network

  /** The path to our encrypted mnemonic seed */
  lazy val seedPath: Path = {
    baseDatadir.resolve(SeedStorage.ENCRYPTED_SEED_FILE_NAME)
  }

  /** Checks if our oracle as a mnemonic seed associated with it */
  def seedExists(): Boolean = {
    SeedStorage.seedExists(seedPath)
  }

  def exists(): Boolean = {
    seedExists() &&
    Files.exists(baseDatadir.resolve("oracle.sqlite"))
  }

  def initialize(krystalBull: KrystalBull): Future[KrystalBull] = {
    val result =
      FutureUtil.foldLeftAsync((), allTables)((_, table) => createTable(table))

    result.failed.foreach(_.printStackTrace())

    result.map(_ => krystalBull)
  }

  def initialize(
      password: AesPassword,
      bip39PasswordOpt: Option[String] = None): Future[KrystalBull] = {
    if (!seedExists()) {
      val entropy = MnemonicCode.getEntropy256Bits
      val mnemonicCode = MnemonicCode.fromEntropy(entropy)
      val decryptedMnemonic = DecryptedMnemonic(mnemonicCode, TimeUtil.now)
      val encrypted = decryptedMnemonic.encrypt(password)
      SeedStorage.writeMnemonicToDisk(seedPath, encrypted)
    }

    val key =
      SeedStorage.getPrivateKeyFromDisk(seedPath, password, bip39PasswordOpt)
    val kb = KrystalBull(key)(this)
    initialize(kb)
  }

  private val rValueTable: TableQuery[Table[_]] = {
    RValueDAO()(ec, appConfig).table
  }

  private val eventTable: TableQuery[Table[_]] = {
    EventDAO()(ec, appConfig).table
  }

  private val eventOutcomeTable: TableQuery[Table[_]] = {
    EventOutcomeDAO()(ec, appConfig).table
  }

  override def allTables: List[TableQuery[Table[_]]] =
    List(rValueTable, eventTable, eventOutcomeTable)

}

object KrystalBullAppConfig {
  val DEFAULT_DATADIR: Path = Paths.get(Properties.userHome, ".krystal-bull")

  def fromDefaultDatadir(confs: Config*)(implicit
      ec: ExecutionContext): KrystalBullAppConfig =
    KrystalBullAppConfig(DEFAULT_DATADIR, confs: _*)
}
