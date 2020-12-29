package com.krystal.bull.gui.home

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.util.ByteString
import com.krystal.bull.gui.GlobalData._
import com.krystal.bull.gui.dialog._
import com.krystal.bull.gui.{GlobalData, TaskRunner}
import org.bitcoins.core.config._
import org.bitcoins.core.currency.{CurrencyUnit, Satoshis}
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.core.protocol.tlv.EventDescriptorTLV
import org.bitcoins.dlc.oracle._
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

import java.time.Instant
import scala.concurrent.Future

case class InitEventParams(
    eventName: String,
    maturationTime: Instant,
    descriptorTLV: EventDescriptorTLV)

class HomePaneModel() {
  var taskRunner: TaskRunner = _

  updateBalance()

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

  implicit val addressChainStatsReads: Reads[AddressChainStats] =
    Json.reads[AddressChainStats]

  implicit val addressStatsReads: Reads[AddressStats] =
    Json.reads[AddressStats]

  private def getBalanceCall(address: BitcoinAddress): Future[CurrencyUnit] = {

    val prefix = GlobalData.network match {
      case MainNet =>
        s"https://blockstream.info/api"
      case TestNet3 =>
        s"https://blockstream.info/testnet/api"
      case net @ (RegTest | SigNet) =>
        throw new IllegalArgumentException(
          s"Unable make an api request on $net")
    }

    val url = prefix ++ s"/address/$address"

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

  def updateBalance(): Unit = {
    val stakingAddress = GlobalData.stakingAddress

    getBalanceCall(stakingAddress).map { amt =>
      GlobalData.stakedAmountText.value = s"${amt.satoshis}"
    }
  }
}
