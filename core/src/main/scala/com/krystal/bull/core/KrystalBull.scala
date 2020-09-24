package com.krystal.bull.core

import com.krystal.bull.core.SigningVersion._
import com.krystal.bull.core.storage._
import org.bitcoins.core.crypto.{ExtPrivateKey, MnemonicCode}
import org.bitcoins.core.hd._
import org.bitcoins.core.protocol.Bech32Address
import org.bitcoins.core.protocol.script.P2WPKHWitnessSPKV0
import org.bitcoins.core.util.TimeUtil
import org.bitcoins.crypto._
import org.bitcoins.keymanager.DecryptedMnemonic
import scodec.bits.ByteVector

import scala.concurrent.{ExecutionContext, Future}

case class KrystalBull(extPrivateKey: ExtPrivateKey)(implicit
    val
    conf: KrystalBullAppConfig) {

  implicit val ec: ExecutionContext = conf.ec

  private val signingKeyHDAddress = {
    val coin = HDCoin(HDPurposes.SegWit, HDCoinType.fromNetwork(conf.network))
    val account = HDAccount(coin, 0)
    val chain = HDChain(HDChainType.External, account)
    HDAddress(chain, 0)
  }

  private val PURPOSE = 585

  private val rValueAccount = {
    // 585 is random one I picked, unclaimed in https://github.com/satoshilabs/slips/blob/master/slip-0044.md
    val coin = HDCoin(HDPurpose(PURPOSE), HDCoinType.fromNetwork(conf.network))
    HDAccount(coin, 0)
  }

  private val signingKey: ECPrivateKey = extPrivateKey
    .deriveChildPrivKey(SegWitHDPath(signingKeyHDAddress))
    .key

  val publicKey: ECPublicKey = signingKey.publicKey

  val stakingAddress: Bech32Address =
    Bech32Address(P2WPKHWitnessSPKV0(signingKey.publicKey), conf.network)

  protected[core] val rValueDAO: RValueDAO = RValueDAO()
  protected[core] val eventDAO: EventDAO = EventDAO()
  protected[core] val eventOutcomeDAO: EventOutcomeDAO = EventOutcomeDAO()

  private def getAuxRand(keyIndex: Int): ECPrivateKey = {
    val coin = HDCoin(HDPurpose(PURPOSE), HDCoinType.fromNetwork(conf.network))
    val account = HDAccount(coin, 0)
    val chain = HDChain(HDChainType.External, account)
    val hdAddress = HDAddress(chain, keyIndex)

    extPrivateKey.deriveChildPrivKey(SegWitHDPath(hdAddress)).key
  }

  def listEvents(): Future[Vector[EventDb]] = eventDAO.findAll()

  def listPendingEvents(): Future[Vector[EventDb]] = eventDAO.getPendingEvents

  def createNewEvent(
      name: String,
      outcomes: Vector[String]): Future[EventDb] = {
    for {
      indexOpt <- rValueDAO.findMostRecent
      index = indexOpt match {
        case Some(value) => value.keyIndex + 1
        case None        => 0
      }

      nonce = getAuxRand(index).schnorrNonce

      rValueDb =
        RValueDbHelper(nonce, rValueAccount, HDChainType.fromInt(0), index)
      eventDb = EventDb(nonce, name, outcomes.size, Mock, None)
      eventOutcomeDbs = outcomes.map { outcome =>
        val hash = CryptoUtil.sha256(ByteVector(outcome.getBytes))
        EventOutcomeDb(nonce, outcome, hash)
      }

      _ <- rValueDAO.create(rValueDb)
      eventDb <- eventDAO.create(eventDb)
      _ <- eventOutcomeDAO.createAll(eventOutcomeDbs)
    } yield eventDb
  }

  def signEvent(
      nonce: SchnorrNonce,
      outcome: String): Future[SchnorrDigitalSignature] = {
    for {
      rValDbOpt <- rValueDAO.read(nonce)
      rValDb = rValDbOpt match {
        case Some(value) => value
        case None =>
          throw new RuntimeException(
            s"Nonce not found from this oracle ${nonce.hex}")
      }

      eventOpt <- eventDAO.read(nonce)
      eventDb = eventOpt match {
        case Some(value) =>
          require(
            value.attestationOpt.isEmpty,
            s"Event already has been signed, attestation: ${value.attestationOpt.get}")
          value
        case None =>
          throw new RuntimeException(
            s"No event saved with nonce ${nonce.hex} $outcome")
      }

      eventOutcomeOpt <- eventOutcomeDAO.read((nonce, outcome))
      eventOutcomeDb = eventOutcomeOpt match {
        case Some(value) => value
        case None =>
          throw new RuntimeException(
            s"No event outcome saved with nonce and message ${nonce.hex} $outcome")
      }

      sig = eventDb.signingVersion match {
        case Mock =>
          val auxRand = getAuxRand(rValDb.keyIndex).bytes
          BouncyCastleUtil.schnorrSign(eventOutcomeDb.hashedMessage.bytes,
                                       signingKey,
                                       auxRand)
      }

      updated = eventDb.copy(attestationOpt = Some(sig.sig))
      _ <- eventDAO.update(updated)
    } yield sig
  }
}

object KrystalBull {

  def fromMnemonicCode(
      mnemonicCode: MnemonicCode,
      password: AesPassword,
      bip39PasswordOpt: Option[String] = None)(implicit
      conf: KrystalBullAppConfig): KrystalBull = {
    val decryptedMnemonic = DecryptedMnemonic(mnemonicCode, TimeUtil.now)
    val encrypted = decryptedMnemonic.encrypt(password)
    SeedStorage.writeMnemonicToDisk(conf.seedPath, encrypted)

    val key =
      SeedStorage.getPrivateKeyFromDisk(conf.seedPath,
                                        password,
                                        bip39PasswordOpt)
    KrystalBull(key)
  }
}
