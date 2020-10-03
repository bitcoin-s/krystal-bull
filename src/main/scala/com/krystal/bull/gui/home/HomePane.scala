package com.krystal.bull.gui.home

import java.time.ZoneId
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.Locale

import com.krystal.bull.gui.GlobalData._
import com.krystal.bull.gui.home.HomePaneModel
import com.krystal.bull.gui.{GlobalData, TaskRunner}
import org.bitcoins.dlc.oracle._
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, GridPane, VBox}
import scalafx.scene.text._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class HomePane(glassPane: VBox) {

  val model = new HomePaneModel()

  private val imageView: ImageView = new ImageView(
    new Image("/icons/krystal_bull.png")) {
    fitHeight = 100
    fitWidth = 100
  }

  def eventStatuses: ObservableBuffer[Event] = {
    val statusF = oracle.listEvents().map { statuses =>
      ObservableBuffer(statuses)
    }
    Await.result(statusF, 5.seconds)
  }

  private val tableView: TableView[Event] = {
    val labelCol = new TableColumn[Event, String] {
      text = "Event Name"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status, "Event Name", status.value.eventName)
      }
    }
    val nonceCol = new TableColumn[Event, String] {
      text = "Nonce"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status, "Nonce", status.value.nonce.hex)
      }
    }
    val maturityDateCol = new TableColumn[Event, String] {
      text = "Maturity Date"
      prefWidth = 150
      cellValueFactory = { status =>
        val formatter =
          DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault())
        new StringProperty(status,
                           "Maturity Date",
                           formatter.format(status.value.maturationTime))
      }
    }
    val numberOutcomesCol = new TableColumn[Event, String] {
      text = "Num Outcomes"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status,
                           "Num Outcomes",
                           status.value.numOutcomes.toString)
      }
    }
    val signingVersionCol = new TableColumn[Event, String] {
      text = "Signing Version"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status,
                           "Signing Version",
                           status.value.signingVersion.toString)
      }
    }
    val signatureCol = new TableColumn[Event, String] {
      text = "Attestation"
      prefWidth = 150
      cellValueFactory = { status =>
        val str = status.value match {
          case completedEvent: CompletedEvent =>
            completedEvent.signature.hex
          case _: PendingEvent => ""
        }
        new StringProperty(status, "Attestation", str)
      }
    }
    new TableView[Event](eventStatuses) {
      alignmentInParent = Pos.Center
      columns ++= Seq(labelCol,
                      nonceCol,
                      maturityDateCol,
                      numberOutcomesCol,
                      signingVersionCol,
                      signatureCol)
      margin = Insets(10, 0, 10, 0)

      val infoItem: MenuItem = new MenuItem("View Event") {
        onAction = _ => {
          val event = selectionModel.value.getSelectedItem
          model.viewEvent(event)
          updateTable()
        }
      }

      contextMenu = new ContextMenu() {
        items += infoItem
      }
    }
  }

  def updateTable(): Unit = {
    tableView.items = eventStatuses
  }

  private val oracleInfoText = new GridPane() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.Center
    vgap = 10
    hgap = 10

    add(new Label("My Public Key:") {
          textAlignment = TextAlignment.Right
        },
        columnIndex = 0,
        rowIndex = 0)
    add(new TextField() {
          text = oracle.publicKey.hex
          editable = false
          minWidth = 500
        },
        columnIndex = 1,
        rowIndex = 0)
    add(new Label("Staking Address:") {
          textAlignment = TextAlignment.Right
        },
        columnIndex = 0,
        rowIndex = 1)
    add(new TextField() {
          text = oracle
            .stakingAddress(GlobalData.network)
            .toString()
          editable = false
          minWidth = 500
        },
        columnIndex = 1,
        rowIndex = 1)
    add(new Label("Staked Amount:") {
          textAlignment = TextAlignment.Right
        },
        columnIndex = 0,
        rowIndex = 2)
    add(new Text() {
          text <== GlobalData.stakedAmountText
          fill <== GlobalData.textColor
        },
        columnIndex = 1,
        rowIndex = 2)
  }

  private val createEventButton = new Button("Create Event") {
    onAction = _ => {
      model.createEvent() match {
        case Some(params) =>
          oracle
            .createNewEvent(params.eventName,
                            params.maturationTime,
                            params.outcomes)
            .map { _ =>
              updateTable()
            }
        case None =>
          ()
      }
    }
  }

  private val centerView = new VBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.TopCenter
    spacing = 100
    children = Vector(oracleInfoText, tableView, createEventButton)
  }

  val view: BorderPane = new BorderPane {
    padding = Insets(top = 10, right = 10, bottom = 10, left = 10)

    top = imageView
    center = centerView
  }

  view.autosize()

  private val taskRunner = new TaskRunner(view, glassPane)
  model.taskRunner = taskRunner
}
