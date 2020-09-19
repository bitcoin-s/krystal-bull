package com.krystal.bull

import com.krystal.bull.storage._
import org.bitcoins.core.hd._
import org.bitcoins.core.protocol.Bech32Address
import org.bitcoins.core.protocol.script.P2WPKHWitnessSPKV0
import org.bitcoins.core.wallet.keymanagement.KeyManagerParams
import org.bitcoins.crypto.{CryptoUtil, SchnorrNonce}
import org.bitcoins.keymanager.bip39.BIP39KeyManager
import scodec.bits.ByteVector

import scala.concurrent.{ExecutionContext, Future}

class KrystalBull(keyManager: BIP39KeyManager)(implicit
    conf: KrystalBullAppConfig) {

  implicit val ec: ExecutionContext = conf.ec

  val kmParams: KeyManagerParams = keyManager.kmParams

  private val addressAccount = {
    val coin = HDCoin(HDPurposes.SegWit, HDCoinType.fromNetwork(conf.network))
    HDAccount(coin, 0)
  }

  private val rValueAccount = {
    // 585 is random one I picked, unclaimed in https://github.com/satoshilabs/slips/blob/master/slip-0044.md
    val coin = HDCoin(HDPurpose(585), HDCoinType.fromNetwork(conf.network))
    HDAccount(coin, 0)
  }

  private val rValueXPub = keyManager.deriveXPub(rValueAccount).get

  private val addressKey = keyManager
    .deriveXPub(addressAccount)
    .get
    .deriveChildPubKey(BIP32Path.fromString("m/0/0"))
    .get
    .key

  val stakingAddress: Bech32Address =
    Bech32Address(P2WPKHWitnessSPKV0(addressKey), conf.network)

  val rValueDAO: RValueDAO = RValueDAO()
  val eventDAO: EventDAO = EventDAO()
  val eventOutcomeDAO: EventOutcomeDAO = EventOutcomeDAO()

  private def getNonce(keyIndex: Int): SchnorrNonce = {
    val key = rValueXPub
      .deriveChildPubKey(BIP32Path.fromString(s"m/0/$keyIndex"))
      .get
      .key
    SchnorrNonce(key.bytes.tail)
  }

  def createNewEvent(
      name: String,
      outcomes: Vector[String]): Future[EventDb] = {

    for {
      indexOpt <- rValueDAO.findMostRecent
      index = indexOpt match {
        case Some(value) => value.keyIndex + 1
        case None        => 0
      }

      nonce = getNonce(index)

      rValueDb =
        RValueDbHelper(nonce, rValueAccount, HDChainType.fromInt(0), index)
      eventDb = EventDb(nonce, name, outcomes.size, None)
      eventOutcomeDbs = outcomes.map { outcome =>
        val hash = CryptoUtil.sha256(ByteVector(outcome.getBytes))
        EventOutcomeDb(nonce, outcome, hash)
      }

      _ <- rValueDAO.create(rValueDb)
      eventDb <- eventDAO.create(eventDb)
      _ <- eventOutcomeDAO.createAll(eventOutcomeDbs)
    } yield eventDb

  }
}
