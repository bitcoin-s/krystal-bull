package com.krystal.bull.core

import java.nio.file.{Path, Paths}

import com.krystal.bull.core.storage.SeedStorage
import com.typesafe.config.Config
import org.bitcoins.core.config.NetworkParameters
import org.bitcoins.core.crypto.MnemonicCode
import org.bitcoins.core.util.{FutureUtil, TimeUtil}
import org.bitcoins.crypto.AesPassword
import org.bitcoins.db.AppConfig
import org.bitcoins.keymanager.DecryptedMnemonic

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
    SeedStorage.seedExists(seedPath)
  }

  def initialize(
      password: AesPassword,
      bip39PasswordOpt: Option[String] = None): KrystalBull = {
    if (!seedExists()) {
      val entropy = MnemonicCode.getEntropy256Bits
      val mnemonicCode = MnemonicCode.fromEntropy(entropy)
      val decryptedMnemonic = DecryptedMnemonic(mnemonicCode, TimeUtil.now)
      val encrypted = decryptedMnemonic.encrypt(password)
      SeedStorage.writeMnemonicToDisk(seedPath, encrypted)
    }

    val key =
      SeedStorage.getPrivateKeyFromDisk(seedPath, password, bip39PasswordOpt)
    KrystalBull(key)(this)
  }
}

object KrystalBullAppConfig {
  val DEFAULT_DATADIR: Path = Paths.get(Properties.userHome, ".krystal-bull")

  def fromDefaultDatadir(confs: Config*)(implicit
      ec: ExecutionContext): KrystalBullAppConfig =
    KrystalBullAppConfig(DEFAULT_DATADIR, confs: _*)
}
