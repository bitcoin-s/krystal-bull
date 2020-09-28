package com.krystal.bull.core.gui.landing

import com.krystal.bull.core.gui.{GlobalData, TaskRunner}
import org.bitcoins.crypto.AesPassword
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, PasswordField}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.text.{Font, TextAlignment}

class LandingPane(glassPane: VBox) {

  val model = new LandingPaneModel()

  private val label: Label = new Label("Welcome to Krystal Bull") {
    alignmentInParent = Pos.BottomCenter
    textAlignment = TextAlignment.Center
    font = new Font(30)
  }

  private val imageView: ImageView = new ImageView(
    new Image("/icons/krystal_bull.png")) {
    alignmentInParent = Pos.Center
    fitHeight = 300
    fitWidth = 300
  }

  private val initializeButton = new Button("Create new oracle") {
    onAction = _ => model.initOracle()
  }

  private val initText = new Label(
    "You do not currently have an oracle wallet!\nYou first need to " +
      "initialize your oracle's wallet by clicking the button bellow.") {
    textAlignment = TextAlignment.Center
  }

  private val initBottom = new VBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.TopCenter
    spacing = 10
    children = Vector(initText, initializeButton)
  }

  private val passwordField = new PasswordField() {
    onKeyReleased = keyEvent => {
      if (keyEvent.getCode == KeyCode.Enter.delegate && text.value.nonEmpty) {
        model.setOracle(AesPassword.fromString(text.value))
      }
    }
  }

  private val unlockButton = new Button("Unlock") {
    onAction = _ =>
      model.setOracle(AesPassword.fromString(passwordField.text.value))
    disable <== passwordField.text.isEmpty
  }

  private val unlockBottom = new HBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.Center
    spacing = 10
    children = Vector(new Label("Password"), passwordField, unlockButton)
  }

  private val bottomBox = if (GlobalData.appConfig.exists()) {
    unlockBottom
  } else {
    initBottom
  }

  val view: BorderPane = new BorderPane {
    padding = Insets(top = 10, right = 10, bottom = 10, left = 10)

    top = label
    center = new VBox() {
      alignment = Pos.Center
      children = Vector(imageView, bottomBox)
    }
  }

  view.autosize()

  private val taskRunner = new TaskRunner(view, glassPane)
  model.taskRunner = taskRunner
}
