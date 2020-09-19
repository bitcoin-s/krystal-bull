package com.krystal.bull.storage

import org.bitcoins.crypto.{FieldElement, SchnorrNonce}

case class EventDb(
    nonce: SchnorrNonce,
    label: String,
    numOutcomes: Long,
    attestationOpt: Option[FieldElement])
