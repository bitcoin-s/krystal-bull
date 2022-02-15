import scala.util.Properties

ThisBuild / scalafmtOnCompile := !Properties.envOrNone("CI").contains("true")

ThisBuild / scalaVersion := "2.13.8"
