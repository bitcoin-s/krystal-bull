package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.GlobalData
import com.krystal.bull.gui.GlobalData._
import org.bitcoins.dlc.oracle._
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.stage.Window

import scala.concurrent.Future

object ViewEventDialog {

  private def showNoOutcomeAlert(): Unit = {
    new Alert(AlertType.Error) {
      initOwner(owner)
      title = "Error!"
      contentText = "Need to choose an outcome to sign"
      dialogPane().stylesheets = GlobalData.currentStyleSheets
    }.showAndWait()
    ()
  }

  private def showConfirmSignAlert(
      outcomeStr: String,
      signFunc: => Future[OracleEvent]): Unit = {
    new Alert(AlertType.Confirmation) {
      initOwner(owner)
      title = "Confirm Signing"
      contentText =
        s"Are you sure you would like sign to the outcome $outcomeStr?"
      dialogPane().stylesheets = GlobalData.currentStyleSheets
    }.showAndWait() match {
      case Some(ButtonType.OK) =>
        signFunc
      case None | Some(_) =>
        ()
    }
  }

  def showAndWait(parentWindow: Window, event: OracleEvent): Unit = {
    val dialog = new Dialog[Unit]() {
      initOwner(parentWindow)
      title = event.eventName
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets

    dialog.dialogPane().content = new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      var row = 0
      add(new Label("Announcement:"), 0, row)
      add(new TextArea() {
            text = event.announcementTLV.hex
            editable = false
            wrapText = true
          },
          columnIndex = 1,
          rowIndex = row)

      row += 1
      add(new Label("Attestations:"), 0, row)
      def addSignButton(nodes: Vector[Node]): Unit = {
        val hBox = new HBox() {
          spacing = 10
          children = nodes
        }

        add(hBox, columnIndex = 1, rowIndex = row)
      }
      event match {
        case completed: CompletedOracleEvent =>
          add(new TextArea() {
                text = completed.signatures.map(_.hex).mkString(", ")
                editable = false
                wrapText = true
              },
              columnIndex = 1,
              rowIndex = row)
        case pendingEnum: PendingEnumV0OracleEvent =>
          var outcomeOpt: Option[String] = None

          val outcomeSelector: ComboBox[String] = new ComboBox(
            pendingEnum.eventDescriptorTLV.outcomes.map(_.normStr)) {
            onAction = (_: ActionEvent) => {
              outcomeOpt = Some(value.value)
            }
          }

          val button = new Button("Sign") {
            onAction = _ =>
              outcomeOpt match {
                case Some(outcome) =>
                  showConfirmSignAlert(
                    outcome,
                    GlobalData.oracle
                      .signEvent(pendingEnum.nonce, EnumAttestation(outcome))
                      .map(_.toOracleEvent))
                case None =>
                  showNoOutcomeAlert()
              }
          }

          addSignButton(Vector(outcomeSelector, button))
        case pendingRange: PendingRangeV0OracleEvent =>
          val outcomeTF = new TextField() {
            promptText = "Outcome"
          }

          def attestationTypeOpt: Option[RangeAttestation] = {
            val str = outcomeTF.text.value
            str.toLongOption.map(RangeAttestation)
          }

          val button = new Button("Sign") {
            onAction = _ =>
              attestationTypeOpt match {
                case Some(outcome) =>
                  showConfirmSignAlert(
                    outcome.outcomeString,
                    GlobalData.oracle
                      .signEvent(pendingRange.eventTLV, outcome)
                      .map(_.toOracleEvent))
                case None =>
                  showNoOutcomeAlert()
              }
          }

          addSignButton(Vector(outcomeTF, button))
        case pendingDecomp: DigitDecompositionV0OracleEvent =>
          val outcomeTF = new TextField() {
            promptText = "Outcome"
          }

          def digitsOpt: Option[Long] = {
            val str = outcomeTF.text.value
            str.toLongOption
          }

          val button = new Button("Sign") {
            onAction = _ =>
              digitsOpt match {
                case Some(outcome) =>
                  showConfirmSignAlert(
                    outcome.toString,
                    GlobalData.oracle
                      .signDigits(pendingDecomp.eventTLV, outcome))
                case None =>
                  showNoOutcomeAlert()
              }
          }

          addSignButton(Vector(outcomeTF, button))
      }
    }

    val _ = dialog.showAndWait()
  }
}
