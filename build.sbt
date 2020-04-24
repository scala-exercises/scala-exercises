import org.scalajs.core.tools.linker.ModuleInitializer
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import play.sbt.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import webscalajs._

addCommandAlias("ci-test", "test")
addCommandAlias("ci-docs", "project-docs/mdoc")

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
      "org.typelevel" %%% "cats-core" % v("catsversion")
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
      "org.scala-exercises" %% "exercises-stdlib"        % v("stdlib") xscalaExercises,
      "org.scala-exercises" %% "exercises-cats"          % v("cats") xscalaExercises,
      "org.scala-exercises" %% "exercises-shapeless"     % v("shapeless") xscalaExercises,
      "org.scala-exercises" %% "exercises-scalatutorial" % v("scalatutorial") xscalaExercises,
      "org.scala-exercises" %% "exercises-fpinscala"     % v("fpinscala") xscalaExercises,
      "org.scala-exercises" %% "exercises-doobie"        % v("doobie") xscalaExercises,
      "org.scala-exercises" %% "exercises-scalacheck"    % v("scalacheck") xscalaExercises,
      "org.scala-exercises" %% "exercises-fetch"         % v("fetch") xscalaExercises,
      "org.scala-exercises" %% "exercises-monocle"       % v("monocle") xscalaExercises,
      "org.scala-exercises" %% "exercises-circe"         % v("circe") xscalaExercises,
      "com.vmunier"                %% "scalajs-scripts"                 % v("scalajsscripts"),
      "com.lihaoyi"                %% "upickle"                         % v("upickle"),
      "org.webjars"                %% "webjars-play"                    % v("webjars"),
      "org.webjars"                % "highlightjs"                      % v("highlightjs"),
      "org.foundweekends"          %% "knockoff"                        % v("knockoff"),
      "com.newrelic.agent.java"    % "newrelic-agent"                   % v("newrelic"),
      "org.typelevel"              %% "cats-effect"                     % v("catsversion"),
      "commons-io"                 % "commons-io"                       % v("commonsio"),
      "org.webjars.bower"          % "bootstrap-sass"                   % v("bootstrap"),
      "com.47deg"                  %% "github4s"                        % v("github4s"),
      "org.scalatest"              %% "scalatest"                       % v("scalatest") % "runtime",
      "org.scalatestplus"          %% "scalatestplus-scalacheck"        % v("scalatestplusScheck") % Test,
      "org.tpolecat"               %% "doobie-core"                     % v("doobieversion"),
      "org.tpolecat"               %% "doobie-hikari"                   % v("doobieversion"),
      "org.tpolecat"               %% "doobie-postgres"                 % v("doobieversion"),
      "com.dimafeng"               %% "testcontainers-scala-scalatest"  % v("testcontainers") % Test,
      "com.dimafeng"               %% "testcontainers-scala-postgresql" % v("testcontainers") % Test,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14"       % v("scalacheckshapeless") % Test,
      "org.tpolecat"               %% "doobie-scalatest"                % v("doobieversion") % Test
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
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      "io.monix"      %%% "monix"     % v("monix"),
      "org.typelevel" %%% "cats-core" % v("catsversion"),
      "com.lihaoyi" %%% "scalatags" % v("scalatags") xscalajs,
      "org.scala-js" %%% "scalajs-dom" % v("scalajsdom"),
      "be.doeraene" %%% "scalajs-jquery" % v("scalajsjquery") xscalajs,
      "com.lihaoyi" %%% "upickle" % v("upickle"),
      "com.lihaoyi" %%% "utest"   % v("utest") % Test
    )
  )

lazy val `evaluator-client` = (project in file("eval-client"))
  .settings(
    name := "evaluator-client",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-blaze-client" % v("http4s"),
      "org.http4s"    %% "http4s-circe"        % v("http4s"),
      "io.circe"      %% "circe-core"          % v("circeversion"),
      "io.circe"      %% "circe-generic"       % v("circeversion"),
      "org.scalatest" %% "scalatest"           % v("scalatest") % Test
    ),
    crossScalaVersions := Seq(scala_212, scala_213)
  )

lazy val clients = Seq(client)

// Definitions

lazy val definitions = (project in file("definitions"))
  .settings(name := "definitions")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"              %% "cats-core"                 % v("catsversion"),
      "org.scalatest"              %% "scalatest"                 % v("scalatest"),
      "org.scalacheck"             %% "scalacheck"                % v("scalacheckversion"),
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % v("scalacheckshapeless")
    ),
    crossScalaVersions := Seq(scala_212, scala_213)
  )

// Runtime

lazy val runtime = (project in file("runtime"))
  .dependsOn(`evaluator-client`)
  .settings(name := "runtime")
  .settings(
    libraryDependencies ++= Seq(
      "org.clapper"   %% "classutil" % v("classutil"),
      "org.typelevel" %% "cats-core" % v("catsversion") % "compile",
      "org.scalatest" %% "scalatest" % v("scalatest") % Test
    ),
    crossScalaVersions := Seq(scala_212, scala_213)
  )

// Compiler

lazy val compiler = (project in file("compiler"))
  .settings(name := "exercise-compiler")
  .settings(
    exportJars := true,
    libraryDependencies ++= Seq(
      "org.scala-lang"         % "scala-compiler"           % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-collection-compat" % v("collectioncompat"),
      "org.typelevel"          %% "cats-core"               % v("catsversion") % "compile",
      "com.47deg"              %% "github4s"                % v("github4s"),
      "org.scalariform"        %% "scalariform"             % v("scalariform"),
      "org.typelevel"          %% "cats-laws"               % v("catsversion") % Test,
      "org.scalatest"          %% "scalatest"               % v("scalatest") % Test
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
      "org.typelevel" %% "cats-core" % v("catsversion") % "compile"
    ),
    scalacOptions += "-Ypartial-unification",
    addCompilerPlugin("org.scalamacros" % "paradise" % v("scalamacros") cross CrossVersion.full),
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

lazy val `project-docs` = (project in file(".docs"))
  .aggregate(server, client, coreJs, coreJvm, runtime, definitions, compiler, `evaluator-client`)
  .settings(moduleName := "server-project-docs")
  .settings(mdocIn := file(".docs"))
  .settings(mdocOut := file("."))
  .settings(skip in publish := true)
  .enablePlugins(MdocPlugin)

///////////////////
// Global settings:
///////////////////

parallelExecution in Global := false

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
