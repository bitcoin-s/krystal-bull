package com.krystal.bull.gui.landing

import com.krystal.bull.gui.{GlobalData, KrystalBullUtil, TaskRunner}
import org.bitcoins.crypto.AesPassword
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, PasswordField}
import scalafx.scene.input.KeyCode
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.text.{Font, TextAlignment}

import java.nio.file.Files

class LandingPane(glassPane: VBox) {

  val model = new LandingPaneModel()

  private val label: Label = new Label("Welcome to Krystal Bull") {
    alignmentInParent = Pos.BottomCenter
    textAlignment = TextAlignment.Center
    font = new Font(30)
  }

  private val logo = KrystalBullUtil.logo(300, Pos.Center)

  private val initText = new Label(
    "You do not currently have an oracle wallet!\nYou first need to " +
      "initialize your oracle's wallet or restore a previous oracle.") {
    textAlignment = TextAlignment.Center
  }

  private val initializeButton = new Button("Create New Oracle") {
    onAction = _ => model.initOracle()
  }

  private val restoreButton = new Button("Restore Oracle") {
    onAction = _ => model.restoreOracle()
  }

  private val buttonBar = new HBox() {
    spacing = 10
    alignmentInParent = Pos.Center
    alignment = Pos.Center
    children = Vector(initializeButton, restoreButton)
  }

  private val initBottom = new VBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.TopCenter
    spacing = 10
    children = Vector(initText, buttonBar)
  }

  private val passwordField = new PasswordField() {
    onKeyReleased = keyEvent => {
      if (keyEvent.getCode == KeyCode.Enter.delegate) {
        val correct = model.loadOracle(AesPassword.fromStringOpt(text.value))

        if (!correct) {
          this.text = ""
        }
      }
    }
  }

  private val unlockButton = new Button("Unlock") {
    onAction = _ => {
      val correct =
        model.loadOracle(AesPassword.fromStringOpt(passwordField.text.value))

      if (!correct) {
        passwordField.text = ""
      }
    }
  }

  private val unlockBottom = new HBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.Center
    spacing = 10
    children = Vector(new Label("Password"), passwordField, unlockButton)
  }

  private val seedExists = Files.exists(GlobalData.seedPath)

  private val bottomBox = {
    if (seedExists) {
      unlockBottom
    } else {
      initBottom
    }
  }

  val view: BorderPane = new BorderPane {
    padding = Insets(top = 10, right = 10, bottom = 10, left = 10)

    top = label
    center = new VBox() {
      alignment = Pos.Center
      children = Vector(logo, bottomBox)
    }
  }

  view.autosize()

  private val taskRunner = new TaskRunner(view, glassPane)
  model.taskRunner = taskRunner
}
