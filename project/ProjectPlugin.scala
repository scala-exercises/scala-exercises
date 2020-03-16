import de.heikoseeberger.sbtheader
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import de.heikoseeberger.sbtheader.License.{ALv2, Custom}
import sbt.Keys._
import sbt._
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import sbtorgpolicies._
import sbtorgpolicies.model._
import scoverage.ScoverageKeys

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = OrgPoliciesPlugin

  object autoImport {

    val scala_212 = "2.12.10"
    val scala_213 = "2.13.1"

    val v: Map[String, String] =
      // Scala Exercises
      Map(
        "stdlib"        -> "0.6.0-SNAPSHOT",
        "cats"          -> "0.6.0-SNAPSHOT",
        "shapeless"     -> "0.6.0-SNAPSHOT",
        "doobie"        -> "0.6.0-SNAPSHOT",
        "scalacheck"    -> "0.6.0-SNAPSHOT",
        "scalatutorial" -> "0.6.0-SNAPSHOT",
        "fpinscala"     -> "0.6.0-SNAPSHOT",
        "circe"         -> "0.6.0-SNAPSHOT",
        "fetch"         -> "0.6.0-SNAPSHOT",
        "monocle"       -> "0.6.0-SNAPSHOT"
      ) ++ Map(
        // JVM Versions
        "catsversion"         -> "2.1.1",
        "collectioncompat"    -> "2.1.4",
        "doobieversion"       -> "0.8.6",
        "bootstrap"           -> "3.3.7",
        "github4s"            -> "0.23.0",
        "classutil"           -> "1.5.1",
        "commonsio"           -> "2.6",
        "highlightjs"         -> "9.15.10",
        "knockoff"            -> "0.8.12",
        "newrelic"            -> "5.9.0",
        "postgres"            -> "42.2.8",
        "scalajsscripts"      -> "1.1.4",
        "scalariform"         -> "0.2.10",
        "scalatest"           -> "3.1.1",
        "scalatestplusScheck" -> "3.1.0.0-RC2",
        "scalacheckversion"   -> "1.14.2",
        "scalacheckshapeless" -> "1.2.4",
        "upickle"             -> "0.7.5",
        "webjars"             -> "2.7.3",
        "scalamacros"         -> "2.1.1",
        "monix"               -> "3.1.0",
        "http4s"              -> "0.21.1",
        "circeversion"        -> "0.12.3",
        "bettermonadicfor"    -> "0.3.1"
      ) ++ Map(
        // JS Versions
        "jquery"        -> "3.4.1",
        "scalajsdom"    -> "0.9.7",
        "scalajsjquery" -> "0.9.5",
        "scalatags"     -> "0.7.0",
        "utest"         -> "0.7.1"
      )

    implicit class Exclude(module: ModuleID) {

      def x(org: String, artifact: String): ModuleID = module.exclude(org, artifact)

      def xscalaz: ModuleID = x("org.scalaz", "scalaz-concurrent")

      def xdoobie: ModuleID = module.excludeAll(ExclusionRule("org.tpolecat"))

      def xscalajs: ModuleID =
        x("org.scala-js", "scalajs-library") x ("org.scala-js", "scalajs-dom")

      def xscalaExercises: ModuleID =
        x("org.scala-exercises", "exercise-compiler") x ("org.scala-exercises", "definitions")
    }

  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      description := "Scala Exercises: The path to enlightenment",
      startYear := Option(2015),
      orgGithubSetting := GitHubSettings(
        organization = "scala-exercises",
        project = name.value,
        organizationName = "Scala Exercises",
        groupId = "org.scala-exercises",
        organizationHomePage = url("https://www.scala-exercises.org"),
        organizationEmail = "hello@47deg.com"
      ),
      orgLicenseSetting := ApacheLicense,
      scalaVersion := scala_213,
      crossScalaVersions := Nil,
      scalaOrganization := "org.scala-lang",
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
      headerLicense := Some(
        ScalaExercisesLicense("2015-2019", "47 Degrees, LLC. <http://www.47deg.com>")),
      ScoverageKeys.coverageFailOnMinimum := false
    ) ++ addCompilerPlugin("com.olegpy" %% "better-monadic-for" % v("bettermonadicfor")) ++ shellPromptSettings

  object ScalaExercisesLicense {

    def apply(yyyy: String, copyrightOwner: String): sbtheader.License = {
      val apacheLicenseText = ALv2(yyyy, copyrightOwner).text

      Custom(s"""| scala-exercises
                 |
                 | $apacheLicenseText
                 |""".stripMargin)
    }

  }
}
