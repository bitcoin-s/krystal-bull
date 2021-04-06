package com.krystal.bull.gui

import com.krystal.bull.gui.settings.Themes
import org.bitcoins.explorer.env.ExplorerEnv
import scalafx.scene.control._
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}

object AppMenuBar {

  def menuBar(model: GUIModel): MenuBar =
    new MenuBar {
      menus = List(new FileMenu().fileMenu,
                   new SettingsMenu().settingsMenu,
                   new ViewMenu().viewMenu,
                   new HelpMenu(model).helpMenu)
    }
}

private class FileMenu() {

  private val quit: MenuItem = new MenuItem("_Quit") {
    mnemonicParsing = true
    accelerator =
      new KeyCodeCombination(KeyCode.Q, KeyCombination.ControlDown) // CTRL + Q
    onAction = _ => sys.exit()
  }

  val fileMenu: Menu =
    new Menu("_File") {
      mnemonicParsing = true
      items = List(quit)
    }
}

private class SettingsMenu() {

  private val advancedMode: MenuItem = new CheckMenuItem("Advanced Mode") {
    selected = GlobalData.advancedMode
    onAction = _ => {
      GlobalData.advancedMode = !GlobalData.advancedMode
      GlobalData.config.writeToFile()
    }
  }

  private val explorerToggle: ToggleGroup = new ToggleGroup()

  private val explorer: Menu = new Menu("_OracleExplorer") {
    mnemonicParsing = true

    private val productionToggle: RadioMenuItem = new RadioMenuItem(
      "_Production") {
      toggleGroup = explorerToggle
      selected = GlobalData.explorerEnv == ExplorerEnv.Production
      id = "prod"
    }

    private val testToggle: RadioMenuItem = new RadioMenuItem("_Test") {
      toggleGroup = explorerToggle
      selected = GlobalData.explorerEnv == ExplorerEnv.Test
      id = "test"
    }

    items = List(productionToggle, testToggle)

    onAction = _ => {
      val selectedId = explorerToggle.selectedToggle.value
        .asInstanceOf[javafx.scene.control.RadioMenuItem]
        .getId

      selectedId match {
        case "prod" =>
          GlobalData.explorerEnv = ExplorerEnv.Production
        case "test" =>
          GlobalData.explorerEnv = ExplorerEnv.Test
        case _: String =>
          throw new RuntimeException("Error, this shouldn't be possible")
      }
      GlobalData.config.writeToFile()
    }
  }

  val settingsMenu: Menu =
    new Menu("_Settings") {
      mnemonicParsing = true
      items = List(advancedMode, explorer)
    }
}

private class ViewMenu() {

  private val themeToggle: ToggleGroup = new ToggleGroup()

  private val themes: Menu = new Menu("_Themes") {
    mnemonicParsing = true

    private val darkThemeToggle: RadioMenuItem = new RadioMenuItem(
      "_Dark Theme") {
      toggleGroup = themeToggle
      selected = GlobalData.darkThemeEnabled
      id = "dark"

      onAction = _ => GlobalData.config.writeToFile()
    }

    private val lightThemeToggle: RadioMenuItem = new RadioMenuItem(
      "_Light Theme") {
      toggleGroup = themeToggle
      selected = !GlobalData.darkThemeEnabled
      id = "light"

      onAction = _ => GlobalData.config.writeToFile()
    }

    items = List(darkThemeToggle, lightThemeToggle)

    onAction = _ => {
      val selectedId = themeToggle.selectedToggle.value
        .asInstanceOf[javafx.scene.control.RadioMenuItem]
        .getId

      selectedId match {
        case "dark" =>
          GlobalData.darkThemeEnabled = true
          Themes.DarkTheme.applyTheme
        case "light" =>
          GlobalData.darkThemeEnabled = false
          Themes.DarkTheme.undoTheme
        case _: String =>
          throw new RuntimeException("Error, this shouldn't be possible")
      }
    }
  }

  val viewMenu: Menu = new Menu("_View") {
    mnemonicParsing = true
    items = List(themes)
  }
}

private class HelpMenu(model: GUIModel) {

  private val about =
    new MenuItem("_About") {
      accelerator = new KeyCodeCombination(KeyCode.F1) // F1
      onAction = _ => model.onAbout()
    }

  val helpMenu: Menu =
    new Menu("_Help") {
      mnemonicParsing = true
      items = List(about)
    }
}
