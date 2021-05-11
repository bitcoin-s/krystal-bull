import sbt.Resolver

name := "krystal-bull"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies ++= Deps.core

fork / run := true

mainClass := Some("com.krystal.bull.gui.GUI")

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

enablePlugins(ReproducibleBuildsPlugin, JavaAppPackaging, GraalVMNativeImagePlugin)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case PathList("reference.conf", _ @ _*) => MergeStrategy.concat
  case _ => MergeStrategy.first
}

scalacOptions ++= Seq("release", "11")