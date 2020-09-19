import scala.util.Properties

scalafmtOnCompile in ThisBuild := !Properties.envOrNone("CI").contains("true")
