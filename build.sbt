import org.scalajs.core.tools.linker.ModuleInitializer
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import play.sbt.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import webscalajs._

lazy val `scala-exercises` = (project in file("."))
  .settings(moduleName := "scala-exercises")
  .settings(noPublishSettings: _*)
  .aggregate(server, client, coreJs, coreJvm, runtime, definitions, compiler, `evaluator-client`)
  .dependsOn(server, client, coreJs, coreJvm, runtime, definitions, compiler, `evaluator-client`)

////////////////////
// Project Modules:
////////////////////

// Purely functional core

lazy val core = (crossProject(JSPlatform, JVMPlatform) in file("core"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % v(Symbol("catsversion"))
    )
  )
  .jsSettings(sharedJsSettings: _*)

lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

// Client and Server projects
lazy val server = (project in file("server"))
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(coreJvm, `evaluator-client`)
  .enablePlugins(PlayScala)
  .enablePlugins(SbtWeb)
  .enablePlugins(JavaAppPackaging)
  .settings(noPublishSettings: _*)
  .settings(
    scalaJSProjects := clients,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(scalaJSProd, gzip),
    routesGenerator := InjectedRoutesGenerator,
    routesImport += "config.Routes._",
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.Specs2, "console")),
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      filters,
      jdbc,
      evolutions,
      cacheApi,
      ws,
      caffeine,
      specs2 xscalaz,
      "org.scala-exercises" %% "runtime"          % version.value changing (),
      "org.scala-exercises" %% "evaluator-client" % version.value changing (),
      "org.scala-exercises" %% "exercises-stdlib" % v(Symbol("stdlib")) xscalaExercises,
      //"org.scala-exercises" %% "exercises-cats"          % v('cats) xscalaExercises,
      //"org.scala-exercises" %% "exercises-shapeless"     % v('shapeless) xscalaExercises,
      //"org.scala-exercises" %% "exercises-scalatutorial" % v('scalatutorial) xscalaExercises,
      //"org.scala-exercises" %% "exercises-fpinscala"     % v('fpinscala) xscalaExercises,
      //"org.scala-exercises" %% "exercises-doobie"        % v('doobie) xscalaExercises,
      //"org.scala-exercises" %% "exercises-scalacheck"    % v('scalacheck) xscalaExercises,
      //"org.scala-exercises" %% "exercises-fetch"         % v('fetch) xscalaExercises,
      //"org.scala-exercises" %% "exercises-monocle"       % v('monocle) xscalaExercises,
      //"org.scala-exercises" %% "exercises-circe"         % v('circe) xscalaExercises,
      "com.vmunier"                %% "scalajs-scripts"           % v(Symbol("scalajsscripts")),
      "com.lihaoyi"                %% "upickle"                   % v(Symbol("upickle")),
      "org.webjars"                %% "webjars-play"              % v(Symbol("webjars")),
      "org.webjars"                % "highlightjs"                % v(Symbol("highlightjs")),
      "org.foundweekends"          %% "knockoff"                  % v(Symbol("knockoff")),
      "com.newrelic.agent.java"    % "newrelic-agent"             % v(Symbol("newrelic")),
      "org.typelevel"              %% "cats-effect"               % v(Symbol("catsversion")),
      "commons-io"                 % "commons-io"                 % v(Symbol("commonsio")),
      "org.webjars.bower"          % "bootstrap-sass"             % v(Symbol("bootstrap")),
      "com.47deg"                  %% "github4s"                  % v(Symbol("github4s")),
      "org.scalatest"              %% "scalatest"                 % v(Symbol("scalatest")) % "runtime",
      "org.scalatestplus"          %% "scalatestplus-scalacheck"  % v(Symbol("scalatestplusScheck")) % Test,
      "org.tpolecat"               %% "doobie-core"               % v(Symbol("doobieversion")),
      "org.tpolecat"               %% "doobie-hikari"             % v(Symbol("doobieversion")),
      "org.tpolecat"               %% "doobie-postgres"           % v(Symbol("doobieversion")),
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % v(Symbol("scalacheckshapeless")) % Test,
      "org.tpolecat"               %% "doobie-specs2"             % v(Symbol("doobieversion")) % Test
    )
  )

lazy val client = (project in file("client"))
  .dependsOn(coreJs)
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .disablePlugins(ScoverageSbtPlugin)
  .settings(name := "client")
  .settings(noPublishSettings: _*)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSMainModuleInitializer := Some(
      ModuleInitializer.mainMethod("org.scalaexercises.client.scripts.ExercisesJS", "main")),
    scalaJSUseMainModuleInitializer in Test := false,
    sourceMappings := SourceMappings.fromFiles(Seq(coreJs.base / "..")),
    scalaJSOptimizerOptions in (Compile, fullOptJS) ~= {
      _.withParallel(false)
    },
    jsDependencies += "org.webjars" % "jquery" % "3.4.1" / "3.4.1/jquery.js",
    skip in packageJSDependencies := false,
    jsEnv := new JSDOMNodeJSEnv(),
    //jsDependencies += RuntimeDOM % Test,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      "io.monix"      %%% "monix"     % v(Symbol("monix")),
      "org.typelevel" %%% "cats-core" % v(Symbol("catsversion")),
      "com.lihaoyi" %%% "scalatags" % v(Symbol("scalatags")) xscalajs,
      "org.scala-js" %%% "scalajs-dom" % v(Symbol("scalajsdom")),
      "be.doeraene" %%% "scalajs-jquery" % v(Symbol("scalajsjquery")) xscalajs,
      "com.lihaoyi" %%% "upickle" % v(Symbol("upickle")),
      "com.lihaoyi" %%% "utest"   % v(Symbol("utest")) % Test
    )
  )

lazy val `evaluator-client` = (project in file("eval-client"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "evaluator-client",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-blaze-client" % v(Symbol("http4s")),
      "org.http4s"    %% "http4s-circe"        % v(Symbol("http4s")),
      "io.circe"      %% "circe-core"          % v(Symbol("circeversion")),
      "io.circe"      %% "circe-generic"       % v(Symbol("circeversion")),
      "org.scalatest" %% "scalatest"           % v(Symbol("scalatest")) % Test
    ),
    crossScalaVersions := Seq(scala_212, scala_213)
  )

lazy val clients = Seq(client)

// Definitions

lazy val definitions = (project in file("definitions"))
  .settings(name := "definitions")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"              %% "cats-core"                 % v(Symbol("catsversion")),
      "org.scalatest"              %% "scalatest"                 % v(Symbol("scalatest")),
      "org.scalacheck"             %% "scalacheck"                % v(Symbol("scalacheckversion")),
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % v(Symbol("scalacheckshapeless"))
    ),
    crossScalaVersions := Seq(scala_212, scala_213)
  )

// Runtime

lazy val runtime = (project in file("runtime"))
  .dependsOn(`evaluator-client`)
  .settings(name := "runtime")
  .settings(
    libraryDependencies ++= Seq(
      "org.clapper"   %% "classutil" % v(Symbol("classutil")),
      "org.typelevel" %% "cats-core" % v(Symbol("catsversion")) % "compile",
      "org.scalatest" %% "scalatest" % v(Symbol("scalatest")) % Test
    ),
    crossScalaVersions := Seq(scala_212, scala_213)
  )

// Compiler

lazy val compiler = (project in file("compiler"))
  .settings(name := "exercise-compiler")
  .settings(
    exportJars := true,
    libraryDependencies ++= Seq(
      "org.scala-lang"  % "scala-compiler" % scalaVersion.value,
      "org.typelevel"   %% "cats-core"     % v(Symbol("catsversion")) % "compile",
      "com.47deg"       %% "github4s"      % v(Symbol("github4s")),
      "org.scalariform" %% "scalariform"   % "0.2.10",
      "org.typelevel"   %% "cats-laws"     % v(Symbol("catsversion")) % Test,
      "org.scalatest"   %% "scalatest"     % v(Symbol("scalatest")) % Test
    ),
    scalacOptions := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => scalacOptions.value ++ Seq("-Ypartial-unification")
        case _             => scalacOptions.value
      }
    },
    crossScalaVersions := Seq(scala_212, scala_213)
  )
  .dependsOn(definitions, runtime)

// Compiler plugin

lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(name := "sbt-exercise")
  .settings(
    scalaVersion := scala_212,
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % v(Symbol("catsversion")) % "compile"
    ),
    scalacOptions += "-Ypartial-unification",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    // Leverage build info to populate compiler classpath--
    // This allows SBT, which currently requires Scala 2.10.x, to load and run
    // the compiler, which requires Scala 2.11.x.
    compilerClasspath := { fullClasspath in (compiler, Compile) }.value,
    buildInfoObject := "Meta",
    buildInfoPackage := "org.scalaexercises.plugin.sbtexercise",
    buildInfoKeys := Seq(
      version,
      BuildInfoKey.map(compilerClasspath) {
        case (_, classFiles) ⇒ ("compilerClasspath", classFiles.map(_.data))
      }
    )
  )
// scripted plugin
  .enablePlugins(SbtPlugin)
  .settings(
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    // Publish definitions before running scripted
    scriptedDependencies := {
      val x = (compile in Test).value
      val y = (publishLocal in definitions).value
      val z = (publishLocal in runtime).value
      ()
    }
  )
  .enablePlugins(BuildInfoPlugin)

///////////////////
// Global settings:
///////////////////

parallelExecution in Global := false

// Disable forking in CI
fork in Test := (System.getenv("CONTINUOUS_INTEGRATION") == null)

pgpPassphrase := Some(getEnvVar("PGP_PASSPHRASE").getOrElse("").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.gpg")
pgpSecretRing := file(s"$gpgFolder/secring.gpg")

lazy val compilerClasspath = TaskKey[Classpath]("compiler-classpath")

// Aliases

addCommandAlias(
  "testAll",
  ";server/test;client/test;definitions/test;runtime/test;compiler/test;sbt-exercise/scripted")
addCommandAlias(
  "publishAll",
  ";definitions/publishLocal;runtime/publishLocal;compiler/publishLocal;sbt-exercise/publishLocal")
addCommandAlias(
  "publishSignedAll",
  ";definitions/publishSigned;runtime/publishSigned;compiler/publishSigned;sbt-exercise/publishSigned;evaluator-client/publishSigned"
)
