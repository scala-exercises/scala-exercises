import sbt.Keys._
import sbt._

trait Dependencies {

  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.0-M15"

  object cats {
    lazy val Version  = "0.4.1"
    lazy val core     = "org.typelevel" %% "cats-core" % Version
  }

  object scala {
    def compiler(scalaVersion: String) = "org.scala-lang" % "scala-compiler" % scalaVersion
  }

  object scalaz {
    lazy val Version    = "7.1.4"
    lazy val core       = "org.scalaz" %% "scalaz-core" % Version
    lazy val concurrent = "org.scalaz" %% "scalaz-concurrent" % Version
  }

}
