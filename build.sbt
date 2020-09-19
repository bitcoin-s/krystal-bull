name := "krystal-bull"

version := "0.1"

scalaVersion := "2.13.3"

resolvers += Resolver.sonatypeRepo("snapshots")

enablePlugins(JavaAppPackaging, GraalVMNativeImagePlugin)

lazy val `krystal-bull` = project
  .in(file("."))
  .aggregate(krystalBull)

lazy val krystalBull = project
  .in(file("krystal-bull"))
  .settings(CommonSettings.settings: _*)
  .settings(
    name := "krystal-bull",
    libraryDependencies ++= Deps.krystalBull
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
