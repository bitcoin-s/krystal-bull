name := "krystal-bull"

version := "0.1"

scalaVersion := "2.13.3"

resolvers += Resolver.sonatypeRepo("snapshots")

enablePlugins(JavaAppPackaging, GraalVMNativeImagePlugin)

lazy val `krystal-bull` = project
  .in(file("."))
  .aggregate(core, gui)
  .settings(CommonSettings.settings: _*)

lazy val core = project
  .in(file("core"))
  .settings(CommonSettings.settings: _*)
  .settings(
    name := "krystal-bull-core",
    libraryDependencies ++= Deps.core
  )

lazy val gui = project
  .in(file("gui"))
  .settings(CommonSettings.settings: _*)
  .settings(
    name := "gui",
    libraryDependencies ++= Deps.gui
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
