package com.krystal.bull.storage

import com.krystal.bull.SigningVersion
import org.bitcoins.crypto.{FieldElement, SchnorrNonce}

case class EventDb(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    signingVersion: SigningVersion,
    attestationOpt: Option[FieldElement])
