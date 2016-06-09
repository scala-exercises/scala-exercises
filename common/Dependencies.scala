/*
 * scala-exercises
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import sbt.Keys._
import sbt._

trait Dependencies {

  object cats {
    val Version  = "0.4.1"
    val core     = "org.typelevel" %% "cats-core" % Version
    val laws     = "org.typelevel" %% "cats-laws" % Version
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
