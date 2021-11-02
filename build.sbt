import com.typesafe.sbt.packager.windows._
import sbt.Resolver

name := "krystal-bull"

version := "0.2"

scalaVersion := "2.13.7"

organization := "org.bitcoin-s"

libraryDependencies ++= Deps.core

fork / run := true

mainClass := Some("com.krystal.bull.gui.GUI")

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

enablePlugins(ReproducibleBuildsPlugin,
              JavaAppPackaging,
              GraalVMNativeImagePlugin,
              WindowsPlugin)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _ @_*)       => MergeStrategy.discard
  case PathList("reference.conf", _ @_*) => MergeStrategy.concat
  case _                                 => MergeStrategy.first
}

Compile / doc := (target.value / "none")
scalacOptions ++= Seq("release", "11", "-Xfatal-warnings") ++
  Seq(
    "-unchecked",
    "-feature",
    "-deprecation",
    "-Ywarn-dead-code",
    "-Ywarn-value-discard",
    "-Ywarn-unused",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ypatmat-exhaust-depth",
    "off"
  )

// general package information (can be scoped to Windows)
maintainer := "Chris Stewart <stewart.chris1234@gmail.com>"
// Will say "Welcome to the <packageSummary> Setup Wizard"
packageSummary := "Krystal Bull"
// Will be used for drop down menu in setup wizard
packageDescription := "Krystal Bull"

// wix build information
wixProductId := java.util.UUID.randomUUID().toString
wixProductUpgradeId := java.util.UUID.randomUUID().toString

// Adding the wanted wixFeature:
wixFeatures += WindowsFeature(
  id = "shortcuts",
  title = "Shortcuts in start menu",
  desc = "Add shortcuts for execution and uninstall in start menu",
  components = Seq(
    AddShortCuts(Seq("bin/krystal-bull.bat"))
  )
)
