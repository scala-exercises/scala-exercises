/*
 * scala-exercises
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import sbt.Keys._
import sbt._

trait Dependencies {

  val scalatest = "org.scalatest" %% "scalatest" % "3.0.0-M15"

  object cats {
    val Version  = "0.4.1"
    val core     = "org.typelevel" %% "cats-core" % Version
  }

  object scala {
    def compiler(scalaVersion: String) = "org.scala-lang" % "scala-compiler" % scalaVersion
  }

  object scalaz {
    val Version    = "7.1.4"
    val core       = "org.scalaz" %% "scalaz-core" % Version
    val concurrent = "org.scalaz" %% "scalaz-concurrent" % Version
  }

}
