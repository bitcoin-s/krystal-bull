package com.krystal.bull.gui.home

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.util.ByteString
import com.krystal.bull.gui.GlobalData._
import com.krystal.bull.gui.dialog._
import com.krystal.bull.gui.{GlobalData, TaskRunner}
import grizzled.slf4j.Logging
import org.bitcoins.commons.serializers.SerializerUtil
import org.bitcoins.core.currency.{CurrencyUnit, Satoshis}
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.core.protocol.tlv.EventDescriptorTLV
import org.bitcoins.dlc.oracle._
import play.api.libs.json._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.stage.Window

import java.time.Instant
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class InitEventParams(
    eventName: String,
    maturationTime: Instant,
    descriptorTLV: EventDescriptorTLV)

class HomePaneModel() extends Logging {
  var taskRunner: TaskRunner = _

  // Sadly, it is a Java "pattern" to pass null into
  // constructors to signal that you want some default
  val parentWindow: ObjectProperty[Window] = {
    ObjectProperty[Window](null.asInstanceOf[Window])
  }

  def createEnumEvent(): Option[InitEventParams] = {
    CreateEnumEventDialog.showAndWait(parentWindow.value)
  }

  def createDigitDecompEvent(): Option[InitEventParams] = {
    CreateDigitDecompEventDialog.showAndWait(parentWindow.value)
  }

  def viewEvent(event: OracleEvent): Unit = {
    ViewEventDialog.showAndWait(parentWindow.value, event)
  }

  case class AddressStats(
      address: BitcoinAddress,
      chain_stats: AddressChainStats,
      mempool_stats: AddressChainStats) {

    val totalReceived: CurrencyUnit =
      chain_stats.funded_txo_sum + mempool_stats.funded_txo_sum

    val totalSpent: CurrencyUnit =
      chain_stats.spent_txo_sum + mempool_stats.spent_txo_sum

    val balance: CurrencyUnit = totalReceived - totalSpent
  }

  case class AddressChainStats(
      funded_txo_count: Int,
      funded_txo_sum: Satoshis,
      spent_txo_count: Int,
      spent_txo_sum: Satoshis)

  implicit object SatoshisReads extends Reads[Satoshis] {

    override def reads(json: JsValue): JsResult[Satoshis] =
      SerializerUtil.processJsNumber[Satoshis](num => Satoshis(num.toBigInt))(
        json)
  }

  implicit object BitcoinAddressReads extends Reads[BitcoinAddress] {

    override def reads(json: JsValue): JsResult[BitcoinAddress] =
      json match {
        case JsString(s) =>
          BitcoinAddress.fromStringT(s) match {
            case Success(address) =>
              JsSuccess(address)
            case Failure(err) =>
              SerializerUtil.buildErrorMsg("address", err)
          }
        case err @ (JsNull | _: JsBoolean | _: JsNumber | _: JsArray |
            _: JsObject) =>
          SerializerUtil.buildJsErrorMsg("jsstring", err)
      }
  }

  implicit val addressChainStatsReads: Reads[AddressChainStats] =
    Json.reads[AddressChainStats]

  implicit val addressStatsReads: Reads[AddressStats] =
    Json.reads[AddressStats]

  private def getBalanceCall(address: BitcoinAddress): Future[CurrencyUnit] = {

    val url = s"https://blockstream.info/api/address/$address"

    Http()
      .singleRequest(Get(url))
      .flatMap(response =>
        response.entity.dataBytes
          .runFold(ByteString.empty)(_ ++ _)
          .map(payload => payload.decodeString(ByteString.UTF_8)))
      .flatMap { str =>
        val json = Json.parse(str)
        json.validate[AddressStats] match {
          case JsSuccess(addressStats, _) =>
            require(addressStats.address == address,
                    "Must receive same address requested")

            Future.successful(addressStats.balance)
          case JsError(error) =>
            Future.failed(
              new RuntimeException(
                s"Unexpected error when parsing response: $error"))
        }
      }
  }

  def updateBalance(): Future[Unit] = {
    GlobalData.stakedAmountTextOpt = Some(StringProperty("Fetching balance..."))

    val stakingAddress = GlobalData.stakingAddress
    logger.info(s"Fetching balance for $stakingAddress")

    getBalanceCall(stakingAddress).map { amt =>
      logger.info(s"Balance for $stakingAddress is ${amt.satoshis}")
      GlobalData.stakedAmountTextOpt.foreach(_.value = s"${amt.satoshis}")
    }
  }
}
