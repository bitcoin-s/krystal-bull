package com.krystal.bull.core.gui

import com.krystal.bull.core.KrystalBull
import com.krystal.bull.core.gui.GlobalData.{appConfig, krystalBullOpt}
import com.krystal.bull.core.gui.home.HomePane
import com.krystal.bull.core.gui.landing.LandingPane
import com.krystal.bull.core.storage.SeedStorage
import org.bitcoins.crypto.AesPassword
import scalafx.application.JFXApp
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, StackPane, VBox}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

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

  private val startingPane = new LandingPane(glassPane).view

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
