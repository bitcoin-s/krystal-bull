package com.krystal.bull.gui

import com.krystal.bull.gui.config.KrystalBullAppConfig
import com.krystal.bull.gui.home.HomePane
import com.krystal.bull.gui.landing.LandingPane
import javafx.application.Platform
import javafx.stage.WindowEvent
import scalafx.application.JFXApp
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, StackPane, VBox}

import scala.util.Properties

object GUI extends JFXApp {
  // Catch unhandled exceptions on FX Application thread
  Thread
    .currentThread()
    .setUncaughtExceptionHandler((_: Thread, ex: Throwable) => {
      ex.printStackTrace()
      val _ = new Alert(AlertType.Error) {
        initOwner(owner)
        title = "Unhandled exception"
        headerText = "Exception: " + ex.getClass + ""
        contentText = Option(ex.getMessage).getOrElse("")
      }.showAndWait()
    })

  if (Properties.isWin) {
    System.setProperty(
      "HOME",
      KrystalBullAppConfig.DEFAULT_DATADIR.getParent.toAbsolutePath.toString)
  }

  private val glassPane = new VBox {
    children = new ProgressIndicator {
      progress = ProgressIndicator.IndeterminateProgress
      visible = true
    }
    alignment = Pos.Center
    visible = false
  }

  private val statusLabel = new Label {
    maxWidth = Double.MaxValue
    padding = Insets(0, 10, 10, 10)
    text <== GlobalData.statusText
  }

  private val model = new GUIModel()

  private val startingPane = new LandingPane(glassPane)(GlobalData.ec).view

  private val borderPane = new BorderPane {
    top = AppMenuBar.menuBar(model)
    center = startingPane
    bottom = statusLabel
  }

  private val rootView = new StackPane {
    children = Seq(borderPane, glassPane)
  }

  val homeScene: Scene = new Scene(1000, 800) {
    root = rootView
    stylesheets = GlobalData.currentStyleSheets
  }

  stage = new JFXApp.PrimaryStage {
    title = "Krystal Bull"
    scene = homeScene
    icons.add(new Image("/icons/krystal_bull.png"))
    minHeight = 400
    minWidth = 400
  }

  stage.sizeToScene()

  def changeToHomeScene(): Unit = {
    borderPane.center = new HomePane(glassPane).view
  }

  override def stopApp(): Unit = {
    sys.exit()
  }
}
