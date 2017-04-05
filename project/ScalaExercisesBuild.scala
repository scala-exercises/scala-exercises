import de.heikoseeberger.sbtheader.{HeaderPattern, HeaderPlugin}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import wartremover.WartRemover.autoImport._
import sbt.Keys._
import sbt._

import scala.util.Try

object ScalaExercisesBuild extends AutoPlugin {

  override def requires = plugins.JvmPlugin && SbtScalariform && HeaderPlugin

  override def trigger = allRequirements

  object autoImport extends deps with settings

  import autoImport._

  def getEnvBooleanValue(envVarName: String) = Try(sys.env(envVarName).toBoolean) getOrElse true

  def guard[T](flag: Boolean)(res: Seq[T]): Seq[T] =
    if (flag) res else Seq.empty

  override def projectSettings = commonSettings ++ miscSettings

  lazy val commonSettings =
    baseSettings ++
      formattingSettings ++
      publishSettings ++
      wartSettings

  lazy val baseSettings = Seq(
    organization := "org.scala-exercises",
    version := v('project),
    scalaVersion := v('scala),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "utf8",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:postfixOps"
    ),

    resolvers ++= Seq(
      Resolver.mavenLocal,
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.url("sbt-plugins", url("https://dl.bintray.com/ssidorenko/sbt-plugins/"))(Resolver.ivyStylePatterns)
    ),

    headers <<= (name, version) { (name, version) => Map(
      "scala" -> (
        HeaderPattern.cStyleBlockComment,
        s"""|/*
            | * scala-exercises-$name
            | * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
            | */
            |
          |""".stripMargin)
    )
    }
  )

  lazy val formattingSettings = guard(getEnvBooleanValue("FORMATCODE")) {
    SbtScalariform.scalariformSettings ++
      Seq(
        ScalariformKeys.preferences := ScalariformKeys.preferences.value
          .setPreference(RewriteArrowSymbols, true)
          .setPreference(AlignParameters, true)
          .setPreference(AlignSingleLineCaseStatements, true)
          .setPreference(DoubleIndentClassDeclaration, true)
          .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
          .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true))
  }

  lazy val wartSettings = guard(getEnvBooleanValue("WARTING")) {
    Seq(wartremoverWarnings in Compile ++= Warts.unsafe)
  }
}
