package com.krystal.bull.gui.home

import java.time.ZoneId
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.Locale

import com.krystal.bull.gui.GlobalData._
import com.krystal.bull.gui.{GlobalData, TaskRunner}
import org.bitcoins.dlc.oracle._
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, GridPane, HBox, VBox}
import scalafx.scene.text._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class HomePane(glassPane: VBox) {

  val model = new HomePaneModel()

  def eventStatuses: ObservableBuffer[OracleEvent] = {
    val statusF = oracle.listEvents().map { statuses =>
      val sorted = statuses.sortBy {
        case _: PendingOracleEvent   => -1
        case _: CompletedOracleEvent => 1
      }
      ObservableBuffer(sorted)
    }
    Await.result(statusF, 5.seconds)
  }

  private val tableView: TableView[OracleEvent] = {
    val labelCol = new TableColumn[OracleEvent, String] {
      text = "Event Name"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status, "Event Name", status.value.eventName)
      }
    }
    val nonceCol = new TableColumn[OracleEvent, String] {
      text = "Announcement"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status,
                           "Announcement",
                           status.value.announcementTLV.hex)
      }
    }
    val maturityDateCol = new TableColumn[OracleEvent, String] {
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
    val signingVersionCol = new TableColumn[OracleEvent, String] {
      text = "Signing Version"
      prefWidth = 150
      cellValueFactory = { status =>
        new StringProperty(status,
                           "Signing Version",
                           status.value.signingVersion.toString)
      }
    }
    val signatureCol = new TableColumn[OracleEvent, String] {
      text = "Attestations"
      prefWidth = 150
      cellValueFactory = { status =>
        val str = status.value match {
          case completedEvent: CompletedOracleEvent =>
            completedEvent.signatures.map(_.hex).mkString(", ")
          case _: PendingOracleEvent => ""
        }
        new StringProperty(status, "Attestations", str)
      }
    }
    new TableView[OracleEvent](eventStatuses) {
      alignmentInParent = Pos.Center
      columns ++= Seq(labelCol,
                      nonceCol,
                      maturityDateCol,
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

  private val createEnumEventButton = new Button("Create Enum Event") {
    onAction = _ => {
      model.createEnumEvent() match {
        case Some(params) =>
          oracle
            .createNewEvent(params.eventName,
                            params.maturationTime,
                            params.descriptorTLV)
            .map { _ =>
              updateTable()
            }
        case None =>
          ()
      }
    }
  }

  private val createRangedEventButton = new Button("Create Ranged Event") {
    onAction = _ => {
      model.createRangedEvent() match {
        case Some(params) =>
          oracle
            .createNewEvent(params.eventName,
                            params.maturationTime,
                            params.descriptorTLV)
            .map { _ =>
              updateTable()
            }
        case None =>
          ()
      }
    }
  }

  private val createDigitDecompEventButton = new Button(
    "Create Digit Decomposition Event") {
    onAction = _ => {
      model.createDigitDecompEvent() match {
        case Some(params) =>
          oracle
            .createNewEvent(params.eventName,
                            params.maturationTime,
                            params.descriptorTLV)
            .map { _ =>
              updateTable()
            }
        case None =>
          ()
      }
    }
  }

  private val createButtons = new HBox() {
    spacing = 10
    alignmentInParent = Pos.TopCenter
    alignment = Pos.TopCenter
    children = Vector(createEnumEventButton,
                      createRangedEventButton,
                      createDigitDecompEventButton)
  }

  private val centerView = new VBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.TopCenter
    spacing = 100
    children = Vector(oracleInfoText, tableView, createButtons)
  }

  val view: BorderPane = new BorderPane {
    padding = Insets(top = 10, right = 10, bottom = 10, left = 10)

    top = GlobalData.logo
    center = centerView
  }

  view.autosize()

  private val taskRunner = new TaskRunner(view, glassPane)
  model.taskRunner = taskRunner
}
