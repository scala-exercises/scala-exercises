import org.scalajs.core.tools.linker.ModuleInitializer
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import play.sbt.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import webscalajs._

addCommandAlias(
  "ci-test",
  ";scalafmtCheckAll; scalafmtSbtCheck; evaluator-client/publishLocal; runtime/publishLocal; coverage; test; coverageReport; coverageAggregate"
)
addCommandAlias("ci-docs", ";github; project-docs/mdoc; headerCreateAll")

lazy val `scala-exercises` = (project in file("."))
  .settings(moduleName := "scala-exercises")
  .settings(skip in publish := true)
  .aggregate(server, client, coreJs, coreJvm, runtime, definitions, compiler, `evaluator-client`)
  .dependsOn(server, client, coreJs, coreJvm, runtime, definitions, compiler, `evaluator-client`)

////////////////////
// Project Modules:
////////////////////

// Purely functional core

lazy val core = (crossProject(JSPlatform, JVMPlatform) in file("core"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % V.catsversion
    )
  )

lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

// Client and Server projects
lazy val server = (project in file("server"))
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(coreJvm, `evaluator-client`)
  .enablePlugins(PlayScala)
  .enablePlugins(SbtWeb)
  .enablePlugins(JavaAppPackaging)
  .settings(skip in publish := true)
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
      "org.scala-exercises"        %% "runtime"                         % version.value changing (),
      "org.scala-exercises"        %% "exercises-stdlib"                % V.exercisesStdlib,
      "org.scala-exercises"        %% "exercises-cats"                  % V.exercisesCats,
      "org.scala-exercises"        %% "exercises-shapeless"             % V.exercisesShapeless,
      "org.scala-exercises"        %% "exercises-scalatutorial"         % V.exercisesScalatutorial,
      "org.scala-exercises"        %% "exercises-fpinscala"             % V.exercisesFpinscala,
      "org.scala-exercises"        %% "exercises-doobie"                % V.exercisesDoobie,
      "org.scala-exercises"        %% "exercises-scalacheck"            % V.exercisesScalacheck,
      "org.scala-exercises"        %% "exercises-fetch"                 % V.exercisesFetch,
      "org.scala-exercises"        %% "exercises-monocle"               % V.exercisesMonocle,
      "org.scala-exercises"        %% "exercises-circe"                 % V.exercisesCirce,
      "com.vmunier"                %% "scalajs-scripts"                 % V.scalajsscripts,
      "com.lihaoyi"                %% "upickle"                         % V.upickle,
      "org.webjars"                %% "webjars-play"                    % V.webjars,
      "org.webjars"                % "highlightjs"                      % V.highlightjs,
      "org.foundweekends"          %% "knockoff"                        % V.knockoff,
      "com.newrelic.agent.java"    % "newrelic-agent"                   % V.newrelic,
      "org.typelevel"              %% "cats-effect"                     % V.catsversion,
      "commons-io"                 % "commons-io"                       % V.commonsio,
      "org.webjars.bower"          % "bootstrap-sass"                   % V.bootstrap,
      "com.47deg"                  %% "github4s"                        % V.github4s,
      "org.scalatest"              %% "scalatest"                       % V.scalatest % Runtime,
      "org.scalatestplus"          %% "scalacheck-1-14"                 % V.scalatestplusScheck % Test,
      "org.tpolecat"               %% "doobie-core"                     % V.doobieversion,
      "org.tpolecat"               %% "doobie-hikari"                   % V.doobieversion,
      "org.tpolecat"               %% "doobie-postgres"                 % V.doobieversion,
      "com.dimafeng"               %% "testcontainers-scala-scalatest"  % V.testcontainers % Test,
      "com.dimafeng"               %% "testcontainers-scala-postgresql" % V.testcontainers % Test,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14"       % V.scalacheckshapeless % Test,
      "org.tpolecat"               %% "doobie-scalatest"                % V.doobieversion % Test
    )
  )

lazy val client = (project in file("client"))
  .dependsOn(coreJs)
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .disablePlugins(ScoverageSbtPlugin)
  .settings(name := "client")
  .settings(skip in publish := true)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSMainModuleInitializer := Some(
      ModuleInitializer.mainMethod("org.scalaexercises.client.scripts.ExercisesJS", "main")
    ),
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
      "io.monix"      %%% "monix"          % V.monix,
      "org.typelevel" %%% "cats-core"      % V.catsversion,
      "com.lihaoyi"   %%% "scalatags"      % V.jsDependencyScalatags,
      "org.scala-js"  %%% "scalajs-dom"    % V.jsDependencyScalajsdom,
      "be.doeraene"   %%% "scalajs-jquery" % V.jsDependencyScalajsjquery,
      "com.lihaoyi"   %%% "upickle"        % V.upickle,
      "com.lihaoyi"   %%% "utest"          % V.jsDependencyUtest % Test
    )
  )

lazy val `evaluator-client` = (project in file("eval-client"))
  .settings(
    name := "evaluator-client",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-blaze-client" % V.http4s,
      "org.http4s"    %% "http4s-circe"        % V.http4s,
      "io.circe"      %% "circe-core"          % V.circeversion,
      "io.circe"      %% "circe-generic"       % V.circeversion,
      "org.scalatest" %% "scalatest"           % V.scalatest % Test
    )
  )

lazy val clients = Seq(client)

// Definitions

lazy val definitions = (project in file("definitions"))
  .settings(name := "definitions")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"              %% "cats-core"                 % V.catsversion,
      "org.scalatest"              %% "scalatest"                 % V.scalatest,
      "org.scalacheck"             %% "scalacheck"                % V.scalacheckversion,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % V.scalacheckshapeless
    )
  )

// Runtime

lazy val runtime = (project in file("runtime"))
  .dependsOn(`evaluator-client`)
  .settings(name := "runtime")
  .settings(
    libraryDependencies ++= Seq(
      "org.clapper"   %% "classutil" % V.classutil,
      "org.typelevel" %% "cats-core" % V.catsversion % Compile,
      "org.scalatest" %% "scalatest" % V.scalatest % Test
    )
  )

// Compiler

lazy val compiler = (project in file("compiler"))
  .settings(name := "exercise-compiler")
  .settings(
    exportJars := true,
    libraryDependencies ++= Seq(
      "org.scala-lang"         % "scala-compiler"           % V.scala,
      "org.scala-lang.modules" %% "scala-collection-compat" % V.collectioncompat,
      "org.typelevel"          %% "cats-core"               % V.catsversion % Compile,
      "com.47deg"              %% "github4s"                % V.github4s,
      "org.scalariform"        %% "scalariform"             % V.scalariform,
      "org.typelevel"          %% "cats-laws"               % V.catsversion % Test,
      "org.scalatest"          %% "scalatest"               % V.scalatest % Test
    )
  )
  .dependsOn(definitions, runtime)

// Compiler plugin

lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(name := "sbt-exercise")
  .settings(
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % V.catsversion % Compile
    ),
    scalacOptions += "-Ypartial-unification",
    addCompilerPlugin("org.scalamacros" % "paradise" % V.scalamacros cross CrossVersion.full),
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
  ";server/test;client/test;definitions/test;runtime/test;compiler/test;sbt-exercise/scripted"
)
addCommandAlias(
  "publishAll",
  ";definitions/publishLocal;runtime/publishLocal;compiler/publishLocal;sbt-exercise/publishLocal"
)
addCommandAlias(
  "publishSignedAll",
  ";definitions/publishSigned;runtime/publishSigned;compiler/publishSigned;sbt-exercise/publishSigned;evaluator-client/publishSigned"
)
