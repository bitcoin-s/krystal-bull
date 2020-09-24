package com.krystal.bull.core.gui.landing

import com.krystal.bull.core.gui.TaskRunner
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, VBox}

class LandingPane(glassPane: VBox) {

  val model = new LandingPaneModel()

  private val label: Label = new Label("Krystal Bull") {
    alignmentInParent = Pos.BottomCenter
  }

  private val imageView: ImageView = new ImageView(
    new Image("/icons/krystal_bull_fit.png")) {
    alignmentInParent = Pos.Center
  }

  private val initializeButton = new Button("Create new oracle") {
    onAction = _ => model.initOracle()
  }

  private val initText = new Label(
    "You do not currently have an oracle wallet! You first need to " +
      "initialize your oracle's wallet by clicking the button bellow.")

  private val initBottom = new VBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.TopCenter
    spacing = 10
    children = Vector(initText, initializeButton)
  }

  val view: BorderPane = new BorderPane {
    padding = Insets(top = 10, right = 10, bottom = 10, left = 10)

    top = label
    center = imageView
    bottom = initBottom
  }

  imageView.fitHeight <== (view.height * 2) / 3
  imageView.fitWidth <== (view.width * 2) / 3

  private val taskRunner = new TaskRunner(view, glassPane)
  model.taskRunner = taskRunner
}
