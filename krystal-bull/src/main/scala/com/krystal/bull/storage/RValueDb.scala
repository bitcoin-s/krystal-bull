package com.krystal.bull.storage

import org.bitcoins.core.hd.{HDAccount, HDChainType, HDCoinType, HDPurpose}
import org.bitcoins.crypto.SchnorrNonce

case class RValueDb(
    nonce: SchnorrNonce,
    purpose: HDPurpose,
    accountCoin: HDCoinType,
    accountIndex: Int,
    chainType: HDChainType,
    keyIndex: Int)

object RValueDbHelper {

  def apply(
      nonce: SchnorrNonce,
      account: HDAccount,
      chainType: HDChainType,
      keyIndex: Int): RValueDb = {
    RValueDb(nonce,
             account.purpose,
             account.coin.coinType,
             account.index,
             chainType,
             keyIndex)
  }
}
