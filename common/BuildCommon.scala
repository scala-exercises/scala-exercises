import sbt.Keys._
import sbt._

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import scala.{ Console => C }

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

  def miscSettings = Seq(
    shellPrompt := { s => s"${C.BLUE}${Project.extract(s).currentProject.id}>${C.RESET} " }
  )

  def dependencySettings = Seq(
    libraryDependencies ++= Seq(
      //"org.scalatest" %% "scalatest" % "3.0.0-M15" % "test"
    )
  )

  override def projectSettings = baseSettings ++ miscSettings ++ formatSettings ++ dependencySettings

  object autoImport {
    def compilelibs(deps: ModuleID*) = deps map (_ % "compile")
    def testlibs(deps: ModuleID*) = deps map (_ % "test")
  }
}
