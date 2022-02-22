import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import com.alejandrohdezma.sbt.github.SbtGithubPlugin

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = SbtGithubPlugin

  object autoImport {

    lazy val V = new {
      val bootstrap: String                 = "3.3.7"
      val cats: String                      = "2.7.0"
      val catsEffect: String                = "3.3.4"
      val commonsio: String                 = "2.11.0"
      val doobie: String                    = "1.0.0-RC2"
      val exercisesCats: String             = "0.6.8+126-cf3d3caa-SNAPSHOT"
      val exercisesCirce: String            = "0.6.6+131-ce6fa0e3-SNAPSHOT"
      val exercisesDoobie: String           = "0.6.6+144-e76c6c0d-SNAPSHOT"
      val exercisesFetch: String            = "0.6.6+126-676d33b1-SNAPSHOT"
      val exercisesFpinscala: String        = "0.6.6+113-16ce2a78-SNAPSHOT"
      val exercisesMonocle: String          = "0.6.6+134-34cfa7c0-SNAPSHOT"
      val exercisesScalacheck: String       = "0.6.6+122-393ff928-SNAPSHOT"
      val exercisesScalatutorial: String    = "0.6.6+114-32bae140-SNAPSHOT"
      val exercisesShapeless: String        = "0.6.6+111-24e86ff1-SNAPSHOT"
      val exercisesStdlib: String           = "0.6.6+122-ee798941-SNAPSHOT"
      val github4s: String                  = "0.30.0"
      val highlightjs: String               = "10.1.2"
      val jsDependencyJquery: String        = "3.4.1"
      val jsDependencyScalajsdom: String    = "1.2.0"
      val jsDependencyScalajsjquery: String = "1.0.0"
      val jsDependencyScalatags: String     = "0.9.4"
      val jsDependencyUtest: String         = "0.7.10"
      val jqueryWebjar: String              = "3.6.0"
      val knockoff: String                  = "0.8.14"
      val monix: String                     = "3.4.0"
      val newrelic: String                  = "5.13.0"
      val postgres: String                  = "42.2.8"
      val runtime: String                   = "0.7.0"
      val scalacheck: String                = "1.14.3"
      val scalacheckShapeless: String       = "1.2.5"
      val scalajsscripts: String            = "1.2.0"
      val scalatest: String                 = "3.2.10"
      val scalatestplusScheck: String       = "3.2.2.0"
      val shapeless: String                 = "2.3.3"
      val testcontainers: String            = "0.39.12"
      val upickle: String                   = "1.4.4"
      val webjars: String                   = "2.8.8-1"
    }

  }

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/scala-exercises/scala-exercises"),
          "scm:git:https://github.com/scala-exercises/scala-exercises.git",
          Some("scm:git:git@github.com:scala-exercises/scala-exercises.git")
        )
      ),
      scalacOptions ~= (_ filterNot (_ == "-Xfuture")),
      javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:-options"),
      (Test / fork)                       := false,
      (Test / parallelExecution)          := false,
      (Global / cancelable)               := true,
      ScoverageKeys.coverageFailOnMinimum := false
    )

}
