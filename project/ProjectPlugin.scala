import de.heikoseeberger.sbtheader.{HeaderPattern, HeaderPlugin}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import sbt.Keys._
import sbt._
import wartremover.WartRemover.autoImport._
import sbtorgpolicies._
import sbtorgpolicies.model._
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import wartremover.Wart

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin && HeaderPlugin && OrgPoliciesPlugin

  object autoImport {

    val v: Map[Symbol, String] =
      // Scala Exercises
      Map(
        'evaluator     -> "0.2.0-SNAPSHOT",
        'stdlib        -> "0.4.0",
        'cats          -> "0.4.0",
        'shapeless     -> "0.4.0",
        'doobie        -> "0.4.0",
        'scalacheck    -> "0.4.0",
        'scalatutorial -> "0.4.0",
        'fpinscala     -> "0.4.0"
      ) ++ Map(
        // JVM Versions
        'bootstrap      -> "3.3.7",
        'classutil      -> "1.1.2",
        'commonsio      -> "2.5",
        'freestyle      -> "0.1.0-SNAPSHOT",
        'highlightjs    -> "9.2.0",
        'knockoff       -> "0.8.3",
        'newrelic       -> "3.29.0",
        'postgres       -> "9.3-1102-jdbc41",
        'scalajsscripts -> "0.2.1",
        'scalariform    -> "0.1.8",
        'upickle        -> "0.2.8",
        'utileval       -> "6.34.0",
        'webjars        -> "2.5.0-4"
      ) ++ Map(
        // JS Versions
        'jquery          -> "2.1.3",
        'scalajsdom      -> "0.9.1",
        'scalajsjquery   -> "0.9.1",
        'scalatags       -> "0.6.3",
        'utest           -> "0.4.5",
        'scalacscoverage -> "1.3.0"
      )

    implicit class Exclude(module: ModuleID) {

      def x(org: String, artifact: String): ModuleID = module.exclude(org, artifact)

      def xscalaz: ModuleID = x("org.scalaz", "scalaz-concurrent")

      def xdoobie: ModuleID = module.excludeAll(ExclusionRule("org.tpolecat"))

      def xscalajs: ModuleID =
        x("org.scala-js", "scalajs-library") x ("org.scala-js", "scalajs-dom")
    }

  }

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
      scalaVersion := "2.11.8",
      scalaOrganization := "org.scala-lang",
      crossScalaVersions := Seq("2.11.8"),
      resolvers ++= Seq(
        Resolver.mavenLocal,
        Resolver.sonatypeRepo("snapshots"),
        Resolver.sonatypeRepo("releases"),
        "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
        "Typesafe Maven Releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
        "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
        Resolver.url("sbt-plugins", url("https://dl.bintray.com/ssidorenko/sbt-plugins/"))(
          Resolver.ivyStylePatterns)
      ),
      scalacOptions += "-Xplugin-require:macroparadise",
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
    ) ++ scalaMacroDependencies ++ shellPromptSettings ++ wartSettings

  lazy val wartSettings: Seq[Def.Setting[Seq[Wart]]] = guard(getEnvVar("WARTING").nonEmpty) {
    Seq(wartremoverWarnings in Compile ++= Warts.unsafe)
  }
}
