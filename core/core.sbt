import sbt.Resolver

name := "krystal-bull"

scalaVersion := "2.13.3"

libraryDependencies ++= Deps.core

resolvers += Resolver.sonatypeRepo("snapshots")

mainClass := Some("com.krystal.bull.core.gui.GUI")
