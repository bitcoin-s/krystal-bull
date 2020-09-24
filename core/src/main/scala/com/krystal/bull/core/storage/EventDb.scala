package com.krystal.bull.core.storage

import com.krystal.bull.core.SigningVersion
import org.bitcoins.crypto.{FieldElement, SchnorrDigitalSignature, SchnorrNonce}

case class EventDb(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    signingVersion: SigningVersion,
    attestationOpt: Option[FieldElement]) {

  lazy val sigOpt: Option[SchnorrDigitalSignature] =
    attestationOpt.map(SchnorrDigitalSignature(nonce, _))
}
