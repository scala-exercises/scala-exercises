import de.heikoseeberger.sbtheader
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import de.heikoseeberger.sbtheader.License.{ALv2, Custom}
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
      val cats: String                      = "2.1.1"
      val circe: String                     = "0.13.0"
      val classutil: String                 = "1.5.1"
      val collectioncompat: String          = "2.1.6"
      val commonsio: String                 = "2.6"
      val doobie: String                    = "0.9.0"
      val exercisesCats: String             = "0.6.0-SNAPSHOT"
      val exercisesCirce: String            = "0.6.0-SNAPSHOT"
      val exercisesDoobie: String           = "0.6.0-SNAPSHOT"
      val exercisesFetch: String            = "0.6.0-SNAPSHOT"
      val exercisesFpinscala: String        = "0.6.0-SNAPSHOT"
      val exercisesMonocle: String          = "0.6.0-SNAPSHOT"
      val exercisesScalacheck: String       = "0.6.0-SNAPSHOT"
      val exercisesScalatutorial: String    = "0.6.0-SNAPSHOT"
      val exercisesShapeless: String        = "0.6.0-SNAPSHOT"
      val exercisesStdlib: String           = "0.6.0-SNAPSHOT"
      val github4s: String                  = "0.24.0"
      val highlightjs: String               = "9.15.10"
      val http4s: String                    = "0.21.3"
      val jsDependencyJquery: String        = "3.4.1"
      val jsDependencyScalajsdom: String    = "0.9.7"
      val jsDependencyScalajsjquery: String = "0.9.5"
      val jsDependencyScalatags: String     = "0.7.0"
      val jsDependencyUtest: String         = "0.7.1"
      val knockoff: String                  = "0.8.12"
      val monix: String                     = "3.1.0"
      val newrelic: String                  = "5.9.0"
      val postgres: String                  = "42.2.8"
      val scala: String                     = "2.13.2"
      val scala212: String                  = "2.12.11"
      val scalacheck: String                = "1.14.3"
      val scalacheckShapeless: String       = "1.2.5"
      val scalajsscripts: String            = "1.1.4"
      val scalamacros: String               = "2.1.1"
      val scalariform: String               = "0.2.10"
      val scalatest: String                 = "3.1.1"
      val scalatestplusScheck: String       = "3.1.1.1"
      val shapeless: String                 = "2.3.3"
      val testcontainers: String            = "0.36.1"
      val upickle: String                   = "0.7.5"
      val webjars: String                   = "2.7.3"
    }

  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      description := "Scala Exercises: The path to enlightenment",
      organization := "org.scala-exercises",
      organizationName := "47 Degrees",
      organizationHomepage := Some(url("https://47deg.com")),
      scalaVersion := autoImport.V.scala,
      resolvers ++= Seq(
        Resolver.mavenLocal,
        Resolver.sonatypeRepo("snapshots"),
        Resolver.sonatypeRepo("releases"),
        Resolver.bintrayIvyRepo("ssidorenko", "sbt-plugins"),
        Resolver.bintrayRepo("eed3si9n", "sbt-plugins"),
        Resolver.typesafeIvyRepo("releases"),
        Resolver.typesafeRepo("releases")
      ),
      scalacOptions ~= (_ filterNot (_ == "-Xfuture")),
      javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:-options"),
      fork in Test := false,
      parallelExecution in Test := false,
      cancelable in Global := true,
      ScoverageKeys.coverageFailOnMinimum := false
    )

}
