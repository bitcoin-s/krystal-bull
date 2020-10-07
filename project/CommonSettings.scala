// these two imports are needed for sbt syntax to work
import sbt.Keys._
import sbt._

object CommonSettings {

  private val isCI = sys.props.get("CI").isDefined

  lazy val settings: Seq[Setting[_]] = List(
//    organization := "",
    homepage := Some(url("https://github.com/benthecarman/krystal-bull")),
    developers := List(
      Developer(
        "benthecarman",
        "Ben Carman",
        "benthecarman@live.com",
        url("https://twitter.com/benthecarman")
      ))
  )
}
