import sbt.Keys._
import sbt._

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import wartremover._

import de.heikoseeberger.sbtheader.HeaderPattern
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderKey.headers

import scala.{ Console => C }
import scala.util.Try

object BuildCommon extends AutoPlugin {
  override def requires = plugins.JvmPlugin && SbtScalariform && HeaderPlugin
  override def trigger = allRequirements

  def baseSettings = Seq(
    organization    := "com.47deg",
    version         := "0.0.0-SNAPSHOT",
    scalaVersion    := { if (!sbtPlugin.value) "2.11.7" else scalaVersion.value },
    scalacOptions   ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    scalacOptions   := {
      if (!sbtPlugin.value) "-Ywarn-unused-import" +: scalacOptions.value
      else scalacOptions.value
    },
    javacOptions    ++= Seq("-encoding", "UTF-8", "-Xlint:-options")
  ) ++ Seq(
    scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
    scalacOptions in (Test, console)    ~= (_ filterNot (_ == "-Ywarn-unused-import"))
  )

  def headerSettings = Seq(
    headers <<= (name, version) { (name, version) => Map(
      "scala" -> (
        HeaderPattern.cStyleBlockComment,
        s"""|/*
            | * scala-exercises-$name
            | * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
            | */
            |
            |""".stripMargin)
    )}
  )

  def formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
      .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
  )

  // `WARTING=false sbt` to drop into SBT w/ wart checking off
  def warting = Try(sys.env("WARTING").toBoolean).getOrElse(true)

  def wartSettings =
    if (warting)
      Seq(wartremoverWarnings in Compile ++= Warts.unsafe)
    else Nil

  def miscSettings = Seq(
    shellPrompt := { s => s"${C.BLUE}${Project.extract(s).currentProject.id}>${C.RESET} " }
  )

  def dependencySettings = Seq(
    libraryDependencies ++= Seq(
      //"org.scalatest" %% "scalatest" % "3.0.0-M15" % "test"
    )
  )

  override def projectSettings =
    baseSettings ++ dependencySettings ++
    formatSettings ++ wartSettings ++
    headerSettings ++
    miscSettings

  object autoImport {    
    def compilelibs(deps: ModuleID*) = deps map (_ % "compile")
    def testlibs(deps: ModuleID*) = deps map (_ % "test")

    object Dep extends Dependencies
  }
}
