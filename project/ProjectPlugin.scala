import de.heikoseeberger.sbtheader
import de.heikoseeberger.sbtheader.HeaderPlugin
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

    val v: Map[Symbol, String] =
      // Scala Exercises
      Map(
        'evaluator     -> "0.5.0-SNAPSHOT",
        'stdlib        -> "0.5.0-SNAPSHOT",
        'cats          -> "0.4.2-SNAPSHOT",
        'shapeless     -> "0.4.2-SNAPSHOT",
        'doobie        -> "0.4.2-SNAPSHOT",
        'scalacheck    -> "0.4.2-SNAPSHOT",
        'scalatutorial -> "0.4.2-SNAPSHOT",
        'fpinscala     -> "0.4.2-SNAPSHOT",
        'circe         -> "0.4.2-SNAPSHOT",
        'fetch         -> "0.4.2-SNAPSHOT",
        'monocle       -> "0.4.2-SNAPSHOT"
      ) ++ Map(
        // JVM Versions
        'bootstrap      -> "3.3.7",
        'classutil      -> "1.1.2",
        'commonsio      -> "2.6",
        'freestyle      -> "0.8.2",
        'highlightjs    -> "9.2.0",
        'knockoff       -> "0.8.12",
        'newrelic       -> "5.6.0",
        'postgres       -> "42.2.8",
        'scalajsscripts -> "1.1.4",
        'scalariform    -> "1.8.3",
        'upickle        -> "0.7.5",
        'webjars        -> "2.7.3",
        'scalamacros    -> "2.1.1",
        'monix          -> "3.0.0"
      ) ++ Map(
        // JS Versions
        'jquery        -> "3.4.1",
        'scalajsdom    -> "0.9.7",
        'scalajsjquery -> "0.9.5",
        'scalatags     -> "0.7.0",
        'utest         -> "0.7.1"
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
      startYear := Option(2016),
      orgGithubSetting := GitHubSettings(
        organization = "scala-exercises",
        project = name.value,
        organizationName = "Scala Exercises",
        groupId = "org.scala-exercises",
        organizationHomePage = url("https://www.scala-exercises.org"),
        organizationEmail = "hello@47deg.com"
      ),
      orgLicenseSetting := ApacheLicense,
      scalaVersion := "2.12.10",
      scalaOrganization := "org.scala-lang",
      crossScalaVersions := Seq("2.12.10"),
      resolvers ++= Seq(
        Resolver.mavenLocal,
        Resolver.sonatypeRepo("snapshots"),
        Resolver.sonatypeRepo("releases"),
        Resolver.bintrayIvyRepo("sbt-plugins", "https://dl.bintray.com/ssidorenko/sbt-plugins/"),
        Resolver.bintrayIvyRepo("scalaz-bintray", "http://dl.bintray.com/scalaz/releases"),
        Resolver.typesafeIvyRepo("http://repo.typesafe.com/typesafe/releases/"),
        Resolver.typesafeRepo("http://repo.typesafe.com/typesafe/maven-releases/")
      ),
      scalacOptions += "-Xplugin-require:macroparadise",
      javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:-options"),
      fork in Test := false,
      parallelExecution in Test := false,
      cancelable in Global := true,
      headerLicense := Some(
        ScalaExercisesLicense("2015-2019", "47 Degrees, LLC. <http://www.47deg.com>")),
      ScoverageKeys.coverageFailOnMinimum := false
    ) ++ addCompilerPlugin("org.scalamacros" % "paradise" % v('scalamacros) cross CrossVersion.full) ++ shellPromptSettings

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
