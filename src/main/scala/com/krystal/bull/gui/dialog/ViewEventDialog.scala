package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.GUIUtil.numberFormatter
import com.krystal.bull.gui.GlobalData._
import com.krystal.bull.gui.{GUIUtil, GlobalData}
import grizzled.slf4j.Logging
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
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.stage.Window

import java.awt.Toolkit.getDefaultToolkit
import java.awt.datatransfer.StringSelection
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object ViewEventDialog extends Logging {

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
          .getAnnouncement(event.announcementTLV)
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
      signFunc: => Future[OracleEvent]): Option[OracleEvent] = {
    new Alert(AlertType.Confirmation) {
      initOwner(owner)
      title = "Confirm Signing"
      contentText =
        s"Are you sure you would like sign to the outcome $outcomeStr?"
      dialogPane().stylesheets = GlobalData.currentStyleSheets
    }.showAndWait() match {
      case Some(ButtonType.OK) =>
        Try(Await.result(signFunc, 10.seconds)).toOption
      case None | Some(_) => None
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
    dialog.resizable = true

    var sigsOpt = event match {
      case _: PendingOracleEvent => None
      case completed: CompletedOracleEvent =>
        Some(completed.oracleAttestmentV0TLV)
    }

    val grid: GridPane = new GridPane() {
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
      val attestationsRow: Int = row
      add(new Label("Attestations:"), 0, attestationsRow)

      def addSignButton(nodes: Vector[Node]): Unit = {
        val hBox = new HBox() {
          spacing = 10
          children = nodes
        }

        add(hBox, columnIndex = 1, rowIndex = attestationsRow)
      }

      def handleCompletedEvent(completed: CompletedOracleEvent): Unit = {
        sigsOpt = Some(completed.oracleAttestmentV0TLV)
        add(new TextField() {
              text = completed.oracleAttestmentV0TLV.hex
              editable = false
              minWidth = 300
            },
            columnIndex = 1,
            rowIndex = attestationsRow)
        add(copyButton(completed.oracleAttestmentV0TLV.hex),
            columnIndex = 2,
            rowIndex = attestationsRow)
        if (GlobalData.advancedMode) {
          add(deleteSigsButton(completed),
              columnIndex = 3,
              rowIndex = attestationsRow)
        }
      }

      def handlePendingEvent(pending: PendingOracleEvent): Unit = {
        pending match {
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
                        .map(_.toOracleEvent)) match {
                      case Some(newEvent) =>
                        children.remove(9)
                        handleCompletedEvent(
                          newEvent.asInstanceOf[CompletedOracleEvent])
                      case None => ()
                    }
                  case None =>
                    showNoOutcomeAlert()
                }
            }

            addSignButton(Vector(outcomeSelector, button))
          case pendingDecomp: PendingDigitDecompositionV0OracleEvent =>
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

                    val newEventOpt =
                      if (
                        outcome < desc.minNum && desc
                          .isInstanceOf[SignedDigitDecompositionEventDescriptor]
                      ) {
                        showOutOfBoundsConfirmSignAlert(outcome = outcome,
                                                        bound = desc.minNum,
                                                        isUpperBound = false,
                                                        signFunc = signFunc)
                        None
                      } else if (
                        outcome < desc.minNum && desc
                          .isInstanceOf[
                            UnsignedDigitDecompositionEventDescriptor]
                      ) {
                        showBelowZeroOutcomeAlert(desc)
                        None
                      } else if (outcome > desc.maxNum) {
                        showOutOfBoundsConfirmSignAlert(outcome = outcome,
                                                        bound = desc.maxNum,
                                                        isUpperBound = true,
                                                        signFunc = signFunc)
                        None
                      } else {
                        showConfirmSignAlert(outcomeStr = outcome.toString,
                                             signFunc = signFunc)
                      }

                    newEventOpt match {
                      case Some(newEvent) =>
                        children.remove(17)
                        handleCompletedEvent(
                          newEvent.asInstanceOf[CompletedOracleEvent])
                      case None => ()
                    }

                    ()
                  case None =>
                    showNoOutcomeAlert()
                }
            }

            addSignButton(Vector(outcomeTF, button))
        }
      }

      event match {
        case completed: CompletedOracleEvent =>
          handleCompletedEvent(completed)
        case pending: PendingOracleEvent =>
          handlePendingEvent(pending)
      }

      oracleNameOpt match {
        case Some(oracleName) =>
          row += 1
          val buttonRow = row
          val sendToExplorerButton = new Button("Send to Oracle Explorer") {
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
              sigsOpt.map(CreateAttestations(event.announcementTLV, _))

            onAction = _ => {
              val needToSendF =
                oracleExplorerClient
                  .getAnnouncement(event.announcementTLV)
                  .map { res =>
                    res.attestations.isDefined != createAttestationsOpt.isDefined
                  }
                  .recover { err =>
                    logger.error("Error retrieving announcement from explorer",
                                 err)
                    true
                  }

              // Recover every function so we can display X and because
              // it will fail if already posted
              lazy val sendF = for {
                _ <-
                  oracleExplorerClient
                    .createAnnouncement(createAnnouncement)
                    .recover(err =>
                      logger.error("Error sending announcement to explorer",
                                   err))
                _ <- createAttestationsOpt match {
                  case Some(attestations) =>
                    oracleExplorerClient
                      .createAttestations(attestations)
                      .recover(err =>
                        logger.error("Error sending attestations to explorer",
                                     err))
                  case None => Future.unit
                }

                success <-
                  oracleExplorerClient
                    .getAnnouncement(event.announcementTLV)
                    .map { res =>
                      res.attestations.isDefined == createAttestationsOpt.isDefined
                    }
                    .recover { err =>
                      logger.error(
                        "Error retrieving announcement from explorer",
                        err)
                      false
                    }
              } yield success

              lazy val errorImgView = {
                val img = new Image("/icons/red-x.png")
                new ImageView(img) {
                  tooltip = Tooltip(
                    "Error, try again later or try posting manually")
                  tooltip.value.setShowDelay(new javafx.util.Duration(100))
                }
              }

              val imgViewF = for {
                needToSend <- needToSendF
                success <-
                  if (needToSend) {
                    sendF
                  } else {
                    logger.info(
                      s"Event (${event.eventName}) already posted, skipping sending..")
                    Future.successful(true)
                  }
              } yield {
                if (success) {
                  val img = new Image("/icons/green-check.png")
                  new ImageView(img)
                } else {
                  logger.error("Failed to send event to oracle explorer")
                  errorImgView
                }
              }

              val imgView = Try(Await.result(imgViewF, 10.seconds)) match {
                case Failure(_)    => errorImgView
                case Success(view) => view
              }
              imgView.fitWidth = 16
              imgView.fitHeight = 16

              add(imgView, 2, buttonRow)

              val baseUrl = explorerEnv.siteUrl
              val url =
                s"${baseUrl}announcement/${event.announcementTLV.sha256.hex}"
              val hyperlink = new Hyperlink(url) {
                maxWidth = 300
              }
              add(hyperlink, 1, buttonRow + 1)

              ()
            }
          }

          add(sendToExplorerButton, 1, buttonRow)
        case None => ()
      }
    }

    dialog.dialogPane().content = grid

    val _ = dialog.showAndWait()
  }
}
