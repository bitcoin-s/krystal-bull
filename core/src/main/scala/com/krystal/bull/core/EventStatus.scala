package com.krystal.bull.core

import org.bitcoins.crypto.{FieldElement, SchnorrDigitalSignature, SchnorrNonce}

trait EventStatus {
  def nonce: SchnorrNonce
  def label: String
  def numOutcomes: Long
  def signingVersion: SigningVersion
}

case class PendingEvent(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    signingVersion: SigningVersion)
    extends EventStatus

case class CompletedEvent(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    signingVersion: SigningVersion,
    attestation: FieldElement)
    extends EventStatus {

  val signature: SchnorrDigitalSignature =
    SchnorrDigitalSignature(nonce, attestation)
}
