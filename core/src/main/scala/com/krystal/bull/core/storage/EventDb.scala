package com.krystal.bull.core.storage

import com.krystal.bull.core.{
  CompletedEvent,
  EventStatus,
  PendingEvent,
  SigningVersion
}
import org.bitcoins.crypto.{FieldElement, SchnorrDigitalSignature, SchnorrNonce}

case class EventDb(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    signingVersion: SigningVersion,
    attestationOpt: Option[FieldElement]) {

  lazy val sigOpt: Option[SchnorrDigitalSignature] =
    attestationOpt.map(SchnorrDigitalSignature(nonce, _))

  lazy val eventStatus: EventStatus = {
    attestationOpt match {
      case Some(sig) =>
        CompletedEvent(nonce, label, numOutcomes, signingVersion, sig)
      case None =>
        PendingEvent(nonce, label, numOutcomes, signingVersion)
    }
  }
}
