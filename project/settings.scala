import de.heikoseeberger.sbtheader.{HeaderPattern, HeaderPlugin}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import scoverage.ScoverageKeys

import com.typesafe.sbt.pgp.PgpKeys._
import sbt.Keys._
import sbt._

import scala.xml.transform.{RewriteRule, RuleTransformer}

trait settings {

  lazy val miscSettings = Seq(
    shellPrompt := { s: State =>
      val c = scala.Console
      val blue = c.RESET + c.BLUE + c.BOLD
      val white = c.RESET + c.BOLD

      val projectName = Project.extract(s).currentProject.id

      s"$blue$projectName$white>${c.RESET}"
    }
  )

  lazy val publishSettings = Seq(
    organizationName := "Scala Exercises",
    organizationHomepage := Some(new URL("https://scala-exercises.org")),
    startYear := Some(2016),
    description := "Scala Exercises: The path to enlightenment",
    homepage := Some(url("https://scala-exercises.org")),
    credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", sys.env.getOrElse("PUBLISH_USERNAME", ""), sys.env.getOrElse("PUBLISH_PASSWORD", "")),
    scmInfo := Some(ScmInfo(url("https://github.com/scala-exercises/site"), "https://github.com/scala-exercises/site.git")),
    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("Snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("Releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )

}
