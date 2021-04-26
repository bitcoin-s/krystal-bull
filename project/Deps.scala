import sbt._

object Deps {

  lazy val osName: String = System.getProperty("os.name") match {
    case n if n.startsWith("Linux")   => "linux"
    case n if n.startsWith("Mac")     => "mac"
    case n if n.startsWith("Windows") => "win"
    case _                            => throw new Exception("Unknown platform!")
  }

  object V {
    val akkaV = "10.2.4"
    val akkaStreamV = "2.6.14"
    val akkaActorV: String = akkaStreamV

    val scalaFxV = "15.0.1-R21"
    val javaFxV = "17-ea+5"
    val bitcoinsV = "0.5.0-183-80f0ecd2-SNAPSHOT"
  }

  object Compile {

    val akkaHttp =
      "com.typesafe.akka" %% "akka-http" % V.akkaV withSources () withJavadoc ()

    val akkaStream =
      "com.typesafe.akka" %% "akka-stream" % V.akkaStreamV withSources () withJavadoc ()

    val akkaActor =
      "com.typesafe.akka" %% "akka-actor" % V.akkaStreamV withSources () withJavadoc ()

    val akkaSlf4j =
      "com.typesafe.akka" %% "akka-slf4j" % V.akkaStreamV withSources () withJavadoc ()

    val bitcoinsOracle =
      "org.bitcoin-s" %% "bitcoin-s-dlc-oracle" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsRpc =
      "org.bitcoin-s" %% "bitcoin-s-bitcoind-rpc" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsOracleExplorer =
      "org.bitcoin-s" %% "bitcoin-s-oracle-explorer-client" % V.bitcoinsV withSources () withJavadoc ()

    val scalaFx =
      "org.scalafx" %% "scalafx" % Deps.V.scalaFxV withSources () withJavadoc ()

    lazy val javaFxBase =
      "org.openjfx" % s"javafx-base" % V.javaFxV classifier osName withSources () withJavadoc ()

    lazy val javaFxControls =
      "org.openjfx" % s"javafx-controls" % V.javaFxV classifier osName withSources () withJavadoc ()

    lazy val javaFxFxml =
      "org.openjfx" % s"javafx-fxml" % V.javaFxV classifier osName withSources () withJavadoc ()

    lazy val javaFxGraphics =
      "org.openjfx" % s"javafx-graphics" % V.javaFxV classifier osName withSources () withJavadoc ()

    lazy val javaFxMedia =
      "org.openjfx" % s"javafx-media" % V.javaFxV classifier osName withSources () withJavadoc ()

    lazy val javaFxSwing =
      "org.openjfx" % s"javafx-swing" % V.javaFxV classifier osName withSources () withJavadoc ()

    lazy val javaFxWeb =
      "org.openjfx" % s"javafx-web" % V.javaFxV classifier osName withSources () withJavadoc ()

    lazy val javaFxDeps = List(javaFxBase,
                               javaFxControls,
                               javaFxFxml,
                               javaFxGraphics,
                               javaFxMedia,
                               javaFxSwing,
                               javaFxWeb)
  }

  val core: List[ModuleID] = List(Compile.bitcoinsOracle,
                                  Compile.bitcoinsOracleExplorer,
                                  Compile.akkaActor,
                                  Compile.akkaHttp,
                                  Compile.akkaStream,
                                  Compile.akkaSlf4j,
                                  Compile.scalaFx) ++ Compile.javaFxDeps

}
