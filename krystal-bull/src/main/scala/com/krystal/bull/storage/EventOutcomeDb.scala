package com.krystal.bull.storage

import org.bitcoins.crypto.{SchnorrNonce, Sha256Digest}

case class EventOutcomeDb(
    nonce: SchnorrNonce,
    message: String,
    hashedMessage: Sha256Digest)
