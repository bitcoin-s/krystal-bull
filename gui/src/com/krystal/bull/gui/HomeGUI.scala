package com.krystal.bull.gui

object HomeGUI extends JFXApp {
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

  val psbtPane = new PSBTsPane(glassPane)

  val psbtTab: Tab = new Tab {
    text = "PSBTs"
    content = psbtPane.view
  }

  val txPane = new TransactionsPane(glassPane)

  val txTab: Tab = new Tab {
    text = "Transactions"
    content = txPane.view
  }

  val cryptoPane = new CryptoPane(glassPane)

  val cryptoTab: Tab = new Tab {
    text = "Crypto"
    content = cryptoPane.view
  }

  val genPane = new GeneratorsPane(glassPane)

  val genTab: Tab = new Tab {
    text = "Generators"
    content = genPane.view
  }

  private val tabPane: TabPane = new TabPane() {

    tabs = Seq(psbtTab, txTab, cryptoTab, genTab)

    tabClosingPolicy = TabClosingPolicy.Unavailable
  }

  private val model = new HomeGUIModel(psbtPane, txPane, genPane)

  private val borderPane = new BorderPane {
    top = AppMenuBar.menuBar(model)
    center = tabPane
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
    title = "PSBT Toolkit"
    scene = homeScene
    icons.add(new Image("/icons/psbt-toolkit.png"))
    minHeight = 400
    minWidth = 400
  }

  stage.sizeToScene()

  override def stopApp(): Unit = {
    sys.exit()
  }
}
