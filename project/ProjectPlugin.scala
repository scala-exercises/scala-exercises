import de.heikoseeberger.sbtheader.{HeaderPattern, HeaderPlugin}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import wartremover.WartRemover.autoImport._

import sbtorgpolicies._
import sbtorgpolicies.model._
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import scala.util.matching.Regex

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin && HeaderPlugin && OrgPoliciesPlugin

  object autoImport {

    val v: Map[Symbol, String] = Map(
      // JVM Versions
      'bootstrap -> "3.2.0",
      'classutil -> "1.0.11",
      'commonsio -> "2.5",
      'evaluator -> "0.2.0-SNAPSHOT",
      'highlightjs -> "9.2.0",
      'knockoff -> "0.8.3",
      'monix -> "2.0.3",
      'newrelic -> "3.29.0",
      'paradise -> "2.1.0",
      'postgres -> "9.3-1102-jdbc41",
      'scalajsscripts -> "0.2.1",
      'scalariform -> "0.1.8",
      'scalaz -> "7.2.4",
      'scalazspecs2 -> "0.4.0",
      'scalacheck -> "1.12.5",
      'scalacheckshapeless -> "0.3.1",
      'scalaTest -> "2.2.6",
      'simulacrum -> "0.8.0",
      'upickle -> "0.2.8",
      'utileval -> "6.34.0",
      'webjars -> "2.3.0"
    ) ++ Map(
      // JS Versions
      'scalajsdom -> "0.8.1",
      'scalatags -> "0.5.2",
      'scalajsjquery -> "0.8.1",
      'utest -> "0.4.5",
      'scalacscoverage -> "1.1.0-JS"
    )

    implicit class Exclude(module: ModuleID) {

      def x(org: String, artifact: String): ModuleID = module.exclude(org, artifact)

      def xscalaz: ModuleID = x("org.scalaz", "scalaz-concurrent")

      def xscalajs: ModuleID = x("org.scala-js", "scalajs-library") x("org.scala-js", "scalajs-dom")
    }

  }

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      name := "scala-exercises",
      description := "Scala Exercises: The path to enlightenment",
      startYear := Option(2016),
      resolvers ++= Seq(
        Resolver.mavenLocal,
        Resolver.sonatypeRepo("snapshots"),
        Resolver.sonatypeRepo("releases"),
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  Resolver.url("sbt-plugins", url("https://dl.bintray.com/ssidorenko/sbt-plugins/"))(Resolver.ivyStylePatterns)),

      orgGithubSetting := GitHubSettings(
        organization = "scala-exercises",
        project = name.value,
        organizationName = "Scala Exercises",
        groupId = "org.scala-exercises",
        organizationHomePage = url("https://www.scala-exercises.org"),
        organizationEmail = "hello@47deg.com"
      ),
      orgLicenseSetting := ApacheLicense,
        scalaVersion := "2.11.8",
      scalaOrganization := "org.scala-lang",
      crossScalaVersions := Seq("2.11.8"),
      javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:-options"),
      fork in Test := false,
      parallelExecution in Test := false,
      cancelable in Global := true,
      headers := Map(
        "scala" -> (HeaderPattern.cStyleBlockComment,
          s"""|/*
              | * scala-exercises - ${name.value}
              | * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
              | */
              |
            |""".stripMargin)
      )
    ) ++ shellPromptSettings ++ wartSettings



  lazy val wartSettings = guard(getEnvVar("WARTING").nonEmpty) {
    Seq(wartremoverWarnings in Compile ++= Warts.unsafe)
  }
}
