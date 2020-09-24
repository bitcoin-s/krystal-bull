package com.krystal.bull.core.gui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, VBox}

class HomePane(glassPane: VBox) {

  val model = new HomePaneModel()
  model.setOracle()

  private val label: Label = new Label("Krystal Bull") {
    alignmentInParent = Pos.BottomCenter
  }

  private val imageView: ImageView = new ImageView(
    new Image("/icons/krystal_bull_fit.png")) {
    alignmentInParent = Pos.Center
  }

  private val oracleInfoStr =
    s"My Public Key: ${GlobalData.krystalBullOpt.get.publicKey}" +
      s"\nStaking Address: ${GlobalData.krystalBullOpt.get.stakingAddress}"

  private val oracleInfoText = new Label(oracleInfoStr)

  private val centerView = new VBox() {
    alignmentInParent = Pos.TopCenter
    alignment = Pos.TopCenter
    spacing = 10
    children = Vector(imageView, oracleInfoText)
  }

  val view: BorderPane = new BorderPane {
    padding = Insets(top = 10, right = 10, bottom = 10, left = 10)

    top = label
    center = centerView
  }

  imageView.fitHeight <== (view.height * 2) / 3
  imageView.fitWidth <== (view.width * 2) / 3

  view.autosize()

  private val taskRunner = new TaskRunner(view, glassPane)
  model.taskRunner = taskRunner
}
