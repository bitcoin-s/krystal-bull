import sbt._

object Deps {

  lazy val osName: String = System.getProperty("os.name") match {
    case n if n.startsWith("Linux")   => "linux"
    case n if n.startsWith("Mac")     => "mac"
    case n if n.startsWith("Windows") => "win"
    case _                            => throw new Exception("Unknown platform!")
  }

  object V {
    val akkav = "10.1.12"
    val akkaStreamv = "2.6.6"
    val akkaActorV: String = akkaStreamv

    val ujsonV = "0.8.0"

    val scodecV = "1.1.17"
    val scalaFxV = "14-R19"
    val bitcoinsV = "0.3.0+462-0a9537d2-SNAPSHOT"
    val javaFxV = "14.0.1"
  }

  object Compile {

    val akkaHttp =
      "com.typesafe.akka" %% "akka-http" % V.akkav withSources () withJavadoc ()

    val akkaStream =
      "com.typesafe.akka" %% "akka-stream" % V.akkaStreamv withSources () withJavadoc ()

    val akkaActor =
      "com.typesafe.akka" %% "akka-actor" % V.akkaStreamv withSources () withJavadoc ()

    val bitcoins =
      "org.bitcoin-s" %% "bitcoin-s-core" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsTestKit =
      "org.bitcoin-s" %% "bitcoin-s-testkit" % V.bitcoinsV withSources () withJavadoc ()

    val ujson = "com.lihaoyi" %% "ujson" % V.ujsonV

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

    val scodec =
      "org.scodec" %% "scodec-bits" % V.scodecV withSources () withJavadoc ()
  }

  val core: List[ModuleID] = List(Compile.akkaActor,
                                  Compile.akkaHttp,
                                  Compile.akkaStream,
                                  Compile.ujson,
                                  Compile.bitcoins,
                                  Compile.bitcoinsTestKit,
                                  Compile.scalaFx) ++ Compile.javaFxDeps

}
