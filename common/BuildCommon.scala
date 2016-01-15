import sbt.Keys._
import sbt._

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import wartremover._

import scala.{ Console => C }
import scala.util.Try

object BuildCommon extends AutoPlugin {
  override def requires = plugins.JvmPlugin && SbtScalariform
  override def trigger = allRequirements

  def baseSettings = Seq(
    organization    := "com.47deg",
    version         := "0.0.0",
    scalaVersion    <<= (sbtPlugin) { isPlugin => if (isPlugin) "2.10.5" else "2.11.7" },
    scalacOptions   ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    javacOptions    ++= Seq("-encoding", "UTF-8", "-Xlint:-options")
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
    miscSettings

  object autoImport {
    def compilelibs(deps: ModuleID*) = deps map (_ % "compile")
    def testlibs(deps: ModuleID*) = deps map (_ % "test")
  }
}
