import sbt.Resolver

name := "krystal-bull"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Deps.core

fork in run := true

mainClass := Some("com.krystal.bull.gui.GUI")

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

enablePlugins(ReproducibleBuildsPlugin, JavaAppPackaging, GraalVMNativeImagePlugin)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
