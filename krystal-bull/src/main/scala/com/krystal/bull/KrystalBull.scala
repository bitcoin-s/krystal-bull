package com.krystal.bull

import com.krystal.bull.storage._

import scala.concurrent.{ExecutionContext, Future}

class KrystalBull(implicit conf: KrystalBullAppConfig) {

  implicit val ec: ExecutionContext = conf.ec

  val rValueDAO: RValueDAO = RValueDAO()
  val eventDAO: EventDAO = EventDAO()
  val eventOutcomeDAO: EventOutcomeDAO = EventOutcomeDAO()
}
