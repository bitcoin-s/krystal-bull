package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.GUIUtil.numberFormatter
import com.krystal.bull.gui.GlobalData._
import com.krystal.bull.gui.{GUIUtil, GlobalData}
import org.bitcoins.core.api.dlcoracle._
import org.bitcoins.core.protocol.tlv._
import org.bitcoins.explorer.model.{
  CreateAnnouncementExplorer,
  CreateAttestations
}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.stage.Window

import java.awt.Toolkit.getDefaultToolkit
import java.awt.datatransfer.StringSelection
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Try

object ViewEventDialog {

  private def copyButton(str: String): Button = {
    new Button() {
      styleClass += "copy-button"
      onAction = _ => {
        val clipboard = getDefaultToolkit.getSystemClipboard
        val sel = new StringSelection(str)
        clipboard.setContents(sel, sel)
      }
    }
  }

  private def deleteSigsButton(event: CompletedOracleEvent): Button = {
    new Button() {
      styleClass += "delete-button"
      onAction = _ => {
        val f = oracleExplorerClient
          .getEvent(event.announcementTLV)
          .map(_.attestations)

        val res = Try(Await.result(f, 5.seconds)).getOrElse(None)

        res match {
          case None    => showDeleteSigsAlert(event)
          case Some(_) => showCannotDeleteSigsAlert(event)
        }
      }
    }
  }

  private def showDeleteSigsAlert(event: CompletedOracleEvent): Unit = {
    new Alert(AlertType.Confirmation) {
      initOwner(owner)
      title = "Warning!"
      contentText =
        s"Deleting signatures for event ${event.eventName} can result in leaking your private key!\n\n" +
          "Only delete signatures if you have not published the previous signatures publicly " +
          "and are absolutely sure you know what you are doing!"
      dialogPane().stylesheets = GlobalData.currentStyleSheets
    }.showAndWait() match {
      case Some(ButtonType.OK) =>
        GlobalData.oracle.deleteAttestations(event.eventTLV)
      case None | Some(_) => ()
    }
  }

  private def showCannotDeleteSigsAlert(event: CompletedOracleEvent): Unit = {
    new Alert(AlertType.Error) {
      initOwner(owner)
      title = "Error!"
      contentText =
        s"Cannot delete signatures for ${event.eventName}. " +
          s"Signatures have already been publicly published and would result in revealing your private key!"
      dialogPane().stylesheets = GlobalData.currentStyleSheets
    }.showAndWait()
    ()
  }

  private def showNoOutcomeAlert(): Unit = {
    new Alert(AlertType.Error) {
      initOwner(owner)
      title = "Error!"
      contentText = "Need to choose an outcome to sign"
      dialogPane().stylesheets = GlobalData.currentStyleSheets
    }.showAndWait()
    ()
  }

  private def showBelowZeroOutcomeAlert(
      descriptor: DigitDecompositionEventDescriptorV0TLV): Unit = {
    new Alert(AlertType.Error) {
      initOwner(owner)
      title = "Error!"
      contentText = "Cannot sign a negative number for this event.\n" +
        s"Bounds for this event: ${descriptor.minNum} - ${descriptor.maxNum}"
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
      case Some(ButtonType.OK) => signFunc
      case None | Some(_)      => ()
    }
  }

  private def showOutOfBoundsConfirmSignAlert(
      outcome: Long,
      bound: BigInt,
      isUpperBound: Boolean,
      signFunc: => Future[OracleEvent]): Unit = {
    val boundStr = if (isUpperBound) "upper" else "lower"
    new Alert(AlertType.Confirmation) {
      initOwner(owner)
      title = "Confirm Signing"
      contentText =
        s"Cannot sign $outcome, it is out of bounds for this event.\n" +
          s"Will instead sign $bound, the $boundStr bound.\n\nPlease confirm."
      dialogPane().stylesheets = GlobalData.currentStyleSheets
    }.showAndWait() match {
      case Some(ButtonType.OK) => signFunc
      case None | Some(_)      => ()
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
      alignmentInParent = Pos.Center

      var row = 0
      add(new Label("Announcement:"), 0, row)
      add(new TextField() {
            text = event.announcementTLV.hex
            editable = false
            minWidth = 300
          },
          columnIndex = 1,
          rowIndex = row)
      add(copyButton(event.announcementTLV.hex),
          columnIndex = 2,
          rowIndex = row)

      row += 1
      add(new Label("Event Name:"), 0, row)
      add(new TextField() {
            text = event.eventName
            editable = false
            minWidth = 300
          },
          columnIndex = 1,
          rowIndex = row)
      add(copyButton(event.eventName), columnIndex = 2, rowIndex = row)

      row += 1
      add(new Label("Maturation Time:"), 0, row)
      add(new TextField() {
            text = event.maturationTime.toString
            editable = false
          },
          columnIndex = 1,
          rowIndex = row)

      event.eventDescriptorTLV match {
        case _: EnumEventDescriptorV0TLV => ()
        case decomp: DigitDecompositionEventDescriptorV0TLV =>
          row += 1
          add(new Label("Min Value:"), 0, row)
          add(new TextField() {
                text = decomp.minNum.toString()
                editable = false
              },
              columnIndex = 1,
              rowIndex = row)

          row += 1
          add(new Label("Max Value:"), 0, row)
          add(new TextField() {
                text = decomp.maxNum.toString
                editable = false
              },
              columnIndex = 1,
              rowIndex = row)

          row += 1
          add(new Label("Unit:"), 0, row)
          add(new TextField() {
                text = decomp.unit
                editable = false
              },
              columnIndex = 1,
              rowIndex = row)

          row += 1
          add(new Label("Precision:"), 0, row)
          add(new TextField() {
                text = decomp.precision.toInt.toString
                editable = false
              },
              columnIndex = 1,
              rowIndex = row)
      }

      event match {
        case _: PendingOracleEvent => ()
        case completed: CompletedOracleEvent =>
          row += 1
          add(new Label("Outcome:"), 0, row)

          val outcomeStr = completed match {
            case enum: CompletedEnumV0OracleEvent =>
              enum.outcome.outcomeString
            case decomp: CompletedDigitDecompositionV0OracleEvent =>
              decomp.outcomeBase10.toString
          }

          add(new TextField() {
                text = outcomeStr
                editable = false
              },
              columnIndex = 1,
              rowIndex = row)
      }

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
          add(new TextField() {
                text = completed.oracleAttestmentV0TLV.hex
                editable = false
                minWidth = 300
              },
              columnIndex = 1,
              rowIndex = row)
          add(copyButton(completed.oracleAttestmentV0TLV.hex),
              columnIndex = 2,
              rowIndex = row)
          if (GlobalData.advancedMode) {
            add(deleteSigsButton(completed), columnIndex = 3, rowIndex = row)
          }
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
        case pendingDecomp: DigitDecompositionV0OracleEvent =>
          val outcomeTF = new TextField() {
            promptText = "Outcome"
          }
          GUIUtil.setNumericInput(outcomeTF)

          def digitsOpt: Option[Long] = {
            Try {
              numberFormatter.parse(outcomeTF.text.value).longValue()
            }.toOption
          }

          val button = new Button("Sign") {
            onAction = _ =>
              digitsOpt match {
                case Some(outcome) =>
                  lazy val signFunc = GlobalData.oracle
                    .signDigits(pendingDecomp.eventTLV, outcome)
                  val desc = pendingDecomp.eventDescriptorTLV

                  if (
                    outcome < desc.minNum && desc
                      .isInstanceOf[SignedDigitDecompositionEventDescriptor]
                  ) {
                    showOutOfBoundsConfirmSignAlert(outcome = outcome,
                                                    bound = desc.minNum,
                                                    isUpperBound = false,
                                                    signFunc = signFunc)
                  } else if (
                    outcome < desc.minNum && desc
                      .isInstanceOf[UnsignedDigitDecompositionEventDescriptor]
                  ) {
                    showBelowZeroOutcomeAlert(desc)
                  } else if (outcome > desc.maxNum) {
                    showOutOfBoundsConfirmSignAlert(outcome = outcome,
                                                    bound = desc.maxNum,
                                                    isUpperBound = true,
                                                    signFunc = signFunc)
                  } else {
                    showConfirmSignAlert(outcomeStr = outcome.toString,
                                         signFunc = signFunc)
                  }
                case None =>
                  showNoOutcomeAlert()
              }
          }

          addSignButton(Vector(outcomeTF, button))
      }

      oracleNameOpt match {
        case Some(oracleName) =>
          row += 1
          val addToExplorerButton = new Button("Add to Oracle Explorer") {
            alignmentInParent = Pos.Center
            private val createAnnouncement: CreateAnnouncementExplorer =
              event.announcementTLV match {
                case v0: OracleAnnouncementV0TLV =>
                  CreateAnnouncementExplorer(oracleAnnouncementV0 = v0,
                                             oracleName = oracleName,
                                             description = event.eventName,
                                             eventURI = None)
              }

            private val createAttestationsOpt: Option[CreateAttestations] =
              event match {
                case _: PendingOracleEvent => None
                case completed: CompletedOracleEvent =>
                  Some(
                    CreateAttestations(completed.announcementTLV,
                                       completed.oracleAttestmentV0TLV))
              }
            onAction = _ => {
              oracleExplorerClient.createAnnouncement(createAnnouncement)
              createAttestationsOpt.foreach(
                oracleExplorerClient.createAttestations)
            }
          }

          add(addToExplorerButton, 1, row)
        case None => ()
      }
    }

    val _ = dialog.showAndWait()
  }
}
