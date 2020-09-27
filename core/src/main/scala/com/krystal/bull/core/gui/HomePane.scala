package com.krystal.bull.core.gui

import com.krystal.bull.core.{CompletedEvent, EventStatus, PendingEvent}
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Label, TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, VBox}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class HomePane(glassPane: VBox) {

  import GlobalData.ec

  val model = new HomePaneModel()
//  GlobalData.krystalBullOpt match {
//    case None    => model.setOracle()
//    case Some(_) => ()
//  }

  private val label: Label = new Label("Krystal Bull") {
    alignmentInParent = Pos.BottomCenter
  }

  private val eventStatuses: ObservableBuffer[EventStatus] = {
    GlobalData.krystalBullOpt match {
      case Some(krystalBull) =>
        val statusF = krystalBull.listEvents().map { eventDbs =>
          val statuses = eventDbs.map(_.eventStatus)
          ObservableBuffer(statuses)
        }
        Await.result(statusF, 5.seconds)
      case None =>
        ObservableBuffer.empty
    }
  }

  private val tableView = {
    val labelCol = new TableColumn[EventStatus, String] {
      text = "Label"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status, "Label", status.value.label)
      }
    }
    val nonceCol = new TableColumn[EventStatus, String] {
      text = "Nonce"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status, "Nonce", status.value.nonce.hex)
      }
    }
    val numberOutcomesCol = new TableColumn[EventStatus, String] {
      text = "Num Outcomes"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status,
                           "Num Outcomes",
                           status.value.numOutcomes.toString)
      }
    }
    val signingVersionCol = new TableColumn[EventStatus, String] {
      text = "Signing Version"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status,
                           "Signing Version",
                           status.value.signingVersion.toString)
      }
    }
    val signatureCol = new TableColumn[EventStatus, String] {
      text = "Signature"
      prefWidth = 150
      cellValueFactory = { status =>
        val str = status.value match {
          case completedEvent: CompletedEvent =>
            completedEvent.signature.hex
          case _: PendingEvent =>
            ""
        }
        new StringProperty(status, "Signature", str)
      }
    }
    new TableView[EventStatus](eventStatuses) {
      alignmentInParent = Pos.Center
      columns ++= Seq(labelCol,
                      nonceCol,
                      numberOutcomesCol,
                      signingVersionCol,
                      signatureCol)
      margin = Insets(10, 0, 10, 0)
//      selectionModel().selectionMode = SelectionMode.Multiple
    }
  }

  private val oracleInfoStr =
    s"My Public Key: ${GlobalData.krystalBullOpt.get.publicKey.hex}" +
      s"\nStaking Address: ${GlobalData.krystalBullOpt.get.stakingAddress(GlobalData.network)}"

  private val oracleInfoText = new Label(oracleInfoStr)

  private val centerView = new VBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.TopCenter
    spacing = 10
    children = Vector(oracleInfoText, tableView)
  }

  val view: BorderPane = new BorderPane {
    padding = Insets(top = 10, right = 10, bottom = 10, left = 10)

    top = label
    center = centerView
  }

  view.autosize()

  private val taskRunner = new TaskRunner(view, glassPane)
  model.taskRunner = taskRunner
}
