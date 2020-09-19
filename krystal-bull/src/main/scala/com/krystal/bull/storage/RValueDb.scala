package com.krystal.bull.storage

import org.bitcoins.core.hd.{HDChainType, HDCoinType, HDPurpose}
import org.bitcoins.crypto.SchnorrNonce

case class RValueDb(
    nonce: SchnorrNonce,
    purpose: HDPurpose,
    accountCoin: HDCoinType,
    accountIndex: Int,
    accountChain: HDChainType,
    addressIndex: Int)
