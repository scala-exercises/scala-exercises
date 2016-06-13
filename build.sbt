import play.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef
import NativePackagerHelper._


// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

// Compiler options
scalacOptions ++= Seq("-feature", "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps")

// Disable forking in CI
fork in Test := (System.getenv("CONTINUOUS_INTEGRATION") == null)

// Common settings

lazy val commonSettings = Seq(
  resolvers ++= Seq(
    "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
    Resolver.sonatypeRepo("snapshots")
  )
)

// Client and Server projects

lazy val clients = Seq(client)
lazy val doobieVersion = "0.2.3"
lazy val scalazVersion = "7.1.4"

lazy val server = (project in file("server"))
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJvm)
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    routesGenerator := InjectedRoutesGenerator,
    routesImport += "config.Routes._",
    scalaVersion := "2.11.7",
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, gzip),
  libraryDependencies ++= Seq(
      filters,
      jdbc,
      evolutions,
      cache,
    ws,
      "org.scala-exercises" %% "runtime" % "0.0.0-SNAPSHOT" changing(),
      "org.scala-exercises" %% "exercises-stdlib" % "0.0.0-SNAPSHOT",
      "org.scala-exercises" %% "exercises-cats" % "0.0.0-SNAPSHOT",
      "org.scala-exercises" %% "exercises-shapeless" % "0.0.0-SNAPSHOT",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "com.vmunier" %% "play-scalajs-scripts" % "0.2.1",
      "com.lihaoyi" %% "upickle" % "0.2.8",
      "org.webjars" %% "webjars-play" % "2.3.0",
      "org.webjars" % "bootstrap-sass" % "3.2.0",
      "org.webjars" % "highlightjs" % "8.7",
      "org.webjars.npm" % "highlight.js" % "9.1.0",
      "com.tristanhunt" %% "knockoff" % "0.8.3",
      "com.fortysevendeg" %% "github4s" % "0.2-SNAPSHOT",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
      "org.scalatest" %% "scalatest" % "2.2.4" % "runtime",
      "org.tpolecat" %% "doobie-core" % doobieVersion exclude("org.scalaz", "scalaz-concurrent"),
      "org.tpolecat" %% "doobie-contrib-hikari" % doobieVersion exclude("org.scalaz", "scalaz-concurrent"),
      "org.tpolecat" %% "doobie-contrib-postgresql" % doobieVersion exclude("org.scalaz", "scalaz-concurrent"),
      specs2,
      "org.typelevel" %% "scalaz-specs2" % "0.3.0" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % "0.3.1" % "test",
      "org.tpolecat" %% "doobie-contrib-specs2" % doobieVersion % "test",
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")))


lazy val client = (project in file("client"))
  .dependsOn(sharedJs)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := "2.11.7",
    persistLauncher := true,
    persistLauncher in Test := false,
    sourceMapsDirectories += sharedJs.base / "..",
    jsDependencies ++= Seq(
      "org.webjars" % "bootstrap" % "3.2.0" / "bootstrap.js" minified "bootstrap.min.js"
    ),
    jsDependencies += RuntimeDOM % "test",
    skip in packageJSDependencies := false,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.1",
      "com.lihaoyi" %%% "scalatags" % "0.5.2",
      "io.monix" %%% "monix" % "2.0-M1",
      "be.doeraene" %%% "scalajs-jquery" % "0.8.1",
      "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
      "com.lihaoyi" %%% "upickle" % "0.2.8",
      "org.typelevel" %%% "cats-core" % "0.4.1"
    )
  )


// Code shared by both the client and server projects

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .jsConfigure(_ enablePlugins ScalaJSPlay)
  .jsSettings(sourceMapsBase := baseDirectory.value / "..",  scalaVersion := "2.11.7")
  .settings(commonSettings: _*)
  .settings(
  scalaVersion := "2.11.7",
  libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "0.4.1" % "compile"
  )
)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// Definitions

lazy val definitions = (project in file("definitions"))
  .settings(publishSettings:_*)
  .settings(
    organization := "org.scala-exercises",
    version := "0.0.0-SNAPSHOT",
    name := "definitions",
    scalaVersion := "2.11.7"
)

// Runtime evaluation

lazy val runtime = (project in file("runtime"))
  .settings(publishSettings:_*)
  .settings(
    organization := "org.scala-exercises",
    version := "0.0.0-SNAPSHOT",
    name := "runtime",
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "org.clapper" %% "classutil" % "1.0.11",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "compile",
      "org.typelevel" %% "cats-core" % "0.4.1" % "compile",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
)

// Compiler & Compiler plugin

lazy val compiler = (project in file("compiler"))
  .settings(publishSettings:_*)
  .settings(
    organization := "org.scala-exercises",
    name := "exercise-compiler",
    version := "0.0.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    exportJars      := true,
    resolvers ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "org.scalariform" %% "scalariform" % "0.1.8",
      "com.fortysevendeg" %% "github4s" % "0.4-SNAPSHOT",
      "org.scala-exercises" %% "runtime" % "0.0.0-SNAPSHOT" changing(),
      "org.scala-exercises" %% "definitions" % "0.0.0-SNAPSHOT" changing(),
      "org.typelevel" %% "cats-core" % "0.4.1" % "compile",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "compile",
      "org.typelevel" %% "cats-laws" % "0.4.1" % "test",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
 )

lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(publishSettings:_*)
  .settings(
    organization := "org.scala-exercises",
    name            := "sbt-exercise",
    version := "0.0.0-SNAPSHOT",
    scalaVersion := "2.10.6",
    sbtPlugin       := true,
    resolvers ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "0.4.1" % "compile"
    ),

    // Leverage build info to populate compiler classpath--
    // This allows SBT, which currently requires Scala 2.10.x, to load and run
    // the compiler, which requires Scala 2.11.x.
    compilerClasspath <<= fullClasspath in (compiler, Compile),
      buildInfoObject   := "Meta",
      buildInfoPackage  := "com.fortysevendeg.exercises.sbtexercise",
      buildInfoKeys     := Seq(
        version,
        BuildInfoKey.map(compilerClasspath) {
          case (_, classFiles) ⇒ ("compilerClasspath", classFiles.map(_.data))
        }
      )
  )
  // scripted plugin
  .settings(ScriptedPlugin.scriptedSettings: _*)
  .settings(
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .enablePlugins(BuildInfoPlugin)

// Distribution

lazy val gpgFolder = sys.env.getOrElse("SE_GPG_FOLDER", ".")

lazy val publishSettings = Seq(
  organizationName := "Scala Exercises",
  organizationHomepage := Some(new URL("http://scala-exercises.org")),
  startYear := Some(2016),
  description := "Scala Exercises: The path to enlightenment",
  homepage := Some(url("http://scala-exercises.org")),
  pgpPassphrase := Some(sys.env.getOrElse("SE_GPG_PASSPHRASE", "").toCharArray),
  pgpPublicRing := file(s"$gpgFolder/pubring.gpg"),
  pgpSecretRing := file(s"$gpgFolder/secring.gpg"),
  credentials += Credentials("Sonatype Nexus Repository Manager",  "oss.sonatype.org",  sys.env.getOrElse("PUBLISH_USERNAME", ""),  sys.env.getOrElse("PUBLISH_PASSWORD", "")),
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
  },
  pomExtra :=
      <developers>
        <developer>
          <id>raulraja</id>
          <name>Raul Raja</name>
          <email>raul@47deg.com</email>
        </developer>
        <developer>
          <id>dialelo</id>
          <name>Alejandro Gómez</name>
          <email>al.g.g@47deg.com</email>
        </developer>
        <developer>
          <id>rafaparadela</id>
          <name>Rafa Paradela</name>
          <email>rafa.p@47deg.com</email>
        </developer>
        <developer>
          <id>MasseGuillaume</id>
          <name>Guillaume Massé</name>
          <email>masgui@gmail.com</email>
        </developer>
      </developers>
)

lazy val compilerClasspath = TaskKey[Classpath]("compiler-classpath")

// Aliases

addCommandAlias("publishAll", ";definitions/publishLocal;runtime/publishLocal;compiler/publishLocal;sbt-exercise/publishLocal")

