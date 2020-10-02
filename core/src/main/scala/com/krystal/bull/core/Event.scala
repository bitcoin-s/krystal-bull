package com.krystal.bull.core

import java.time.Instant

import com.krystal.bull.core.storage.{EventDb, EventOutcomeDb, RValueDb}
import org.bitcoins.commons.jsonmodels.dlc.SigningVersion
import org.bitcoins.crypto.{FieldElement, SchnorrDigitalSignature, SchnorrNonce}

sealed trait Event {
  def nonce: SchnorrNonce
  def label: String
  def numOutcomes: Long
  def signingVersion: SigningVersion
  def maturationTime: Instant
  def commitmentSignature: SchnorrDigitalSignature
  def outcomes: Vector[String]
}

case class PendingEvent(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    signingVersion: SigningVersion,
    maturationTime: Instant,
    commitmentSignature: SchnorrDigitalSignature,
    outcomes: Vector[String])
    extends Event

case class CompletedEvent(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    signingVersion: SigningVersion,
    maturationTime: Instant,
    commitmentSignature: SchnorrDigitalSignature,
    outcomes: Vector[String],
    attestation: FieldElement)
    extends Event {

  val signature: SchnorrDigitalSignature =
    SchnorrDigitalSignature(nonce, attestation)
}

object Event {

  def apply(
      rValueDb: RValueDb,
      eventDb: EventDb,
      outcomeDbs: Vector[EventOutcomeDb]): Event = {
    val outcomes = outcomeDbs.map(_.message)

    eventDb.attestationOpt match {
      case Some(sig) =>
        CompletedEvent(eventDb.nonce,
                       eventDb.label,
                       eventDb.numOutcomes,
                       eventDb.signingVersion,
                       eventDb.maturationTime,
                       rValueDb.commitmentSignature,
                       outcomes,
                       sig)
      case None =>
        PendingEvent(eventDb.nonce,
                     eventDb.label,
                     eventDb.numOutcomes,
                     eventDb.signingVersion,
                     eventDb.maturationTime,
                     rValueDb.commitmentSignature,
                     outcomes)
    }
  }
}
