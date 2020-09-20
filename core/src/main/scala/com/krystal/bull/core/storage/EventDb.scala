package com.krystal.bull.core.storage

import com.krystal.bull.core.SigningVersion
import org.bitcoins.crypto.{FieldElement, SchnorrNonce}

case class EventDb(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    signingVersion: SigningVersion,
    attestationOpt: Option[FieldElement])
