import play.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef
import webscalajs._

////////////////////
// Project Modules:
////////////////////

// Purely functional core

lazy val core = (crossProject.crossType(CrossType.Pure) in file("core"))
  .jsConfigure(_ enablePlugins ScalaJSWeb)
  .jsSettings(
    sourceMappings := SourceMappings.fromFiles(Seq(baseDirectory.value / ".."))
  )
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      %%("cats-core"),
      %%("cats-free"),
      compilerPlugin(%%("kind-projector"))
    )
  )

lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

// Client and Server projects

lazy val server = (project in file("server"))
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(core.jvm)
  .enablePlugins(PlayScala)
  .settings(
    routesGenerator := InjectedRoutesGenerator,
    routesImport += "config.Routes._",
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, gzip),
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.Specs2, "console")),
    libraryDependencies ++= Seq(
      filters,
      jdbc,
      evolutions,
      cache,
      ws,
      "org.scala-exercises"     %% "exercises-stdlib"        % version.value changing (),
      "org.scala-exercises"     %% "exercises-cats"          % version.value changing (),
      "org.scala-exercises"     %% "exercises-shapeless"     % version.value changing (),
      "org.scala-exercises"     %% "exercises-doobie"        % version.value changing (),
      "org.scala-exercises"     %% "exercises-scalacheck"    % version.value changing (),
      "org.scala-exercises"     %% "exercises-fpinscala"     % version.value changing (),
      "org.scala-exercises"     %% "exercises-scalatutorial" % version.value changing (),
      "org.scala-exercises"     %% "runtime"                 % version.value changing (),
      "org.scala-exercises"     %% "evaluator-client"        % v('evaluator) changing (),
      "org.postgresql"          % "postgresql"               % v('postgres),
      "com.vmunier"             %% "play-scalajs-scripts"    % v('scalajsscripts),
      "com.lihaoyi"             %% "upickle"                 % v('upickle),
      "org.webjars"             %% "webjars-play"            % v('webjars),
      "org.webjars"             % "bootstrap-sass"           % v('bootstrap),
      "org.webjars"             % "highlightjs"              % v('highlightjs),
      "com.tristanhunt"         %% "knockoff"                % v('knockoff),
      "com.newrelic.agent.java" % "newrelic-agent"           % v('newrelic),
      "commons-io"              % "commons-io"               % v('commonsio),
      "com.47deg"               %% "freestyle"               % v('freestyle),
      %%("github4s"),
      %("slf4j-nop"),
      %%("scalaz-concurrent"),
      %%("scalatest") % "runtime",
      %%("doobie-core"),
      %%("doobie-hikari"),
      %%("doobie-postgres"),
      %%("simulacrum"),
      specs2 xscalaz,
      %%("scheckShapeless") % "test",
      %%("doobie-specs2")   % "test",
      %%("scalazspecs2")    % "test",
      compilerPlugin(%%("kind-projector"))
    )
  )
  .settings(scalaMacroDependencies: _*)

lazy val client = (project in file("client"))
  .dependsOn(core.js)
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    sourceMappings := SourceMappings.fromFiles(Seq(core.js.base / "..")),
    scalaJSOptimizerOptions in (Compile, fullOptJS) ~= {
      _.withParallel(false)
    },
    jsDependencies ++= Seq(
      "org.webjars" % "bootstrap" % v('bootstrap) / "bootstrap.js" minified "bootstrap.min.js"
    ),
    jsDependencies += RuntimeDOM % "test",
    skip in packageJSDependencies := false,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      %%%("monix"),
      %%%("monix-cats"),
      %%%("cats-core"),
      "com.lihaoyi" %%% "scalatags" % v('scalatags) xscalajs,
      "org.scala-js" %%% "scalajs-dom" % v('scalajsdom),
      "be.doeraene" %%% "scalajs-jquery" % v('scalajsjquery) xscalajs,
      "com.lihaoyi"            %%% "utest"                    % v('utest) % "test",
      "com.lihaoyi"            %%% "upickle"                  % v('upickle),
      "ch.sidorenko.scoverage" %%% "scalac-scoverage-runtime" % v('scalacscoverage)
    )
  )

lazy val clients = Seq(client)

// Definitions

lazy val definitions = (project in file("definitions"))
  .settings(
    name := "definitions",
    libraryDependencies ++= Seq(
      %%("cats-core"),
      %%("scalatest"),
      %%("scalacheck"),
      %%("scheckShapeless")
    )
  )

// Runtime

lazy val runtime = (project in file("runtime"))
  .settings(
    name := "runtime",
    libraryDependencies ++= Seq(
      "org.clapper"         %% "classutil"        % v('classutil),
      "com.twitter"         %% "util-eval"        % v('utileval),
      "org.scala-exercises" %% "evaluator-shared" % v('evaluator) changing (),
      %%%("monix"),
      %%("cats-core") % "compile",
      %%("scalatest") % "test"
    )
  )

// Compiler

lazy val compiler = (project in file("compiler"))
  .settings(
    name := "exercise-compiler",
    exportJars := true,
    libraryDependencies ++= Seq(
      "org.scalariform" %% "scalariform"   % v('scalariform),
      "org.scala-lang"  % "scala-compiler" % scalaVersion.value % "compile",
      %%("github4s"),
      %%("cats-core") % "compile",
      %%("cats-laws") % "test",
      %%("scalatest") % "test"
    )
  )
  .dependsOn(definitions, runtime)

// Compiler plugin

lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(
    name := "sbt-exercise",
    scalaVersion := "2.10.6",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      %%("cats-core") % "compile"
    ),
    // Leverage build info to populate compiler classpath--
    // This allows SBT, which currently requires Scala 2.10.x, to load and run
    // the compiler, which requires Scala 2.11.x.
    compilerClasspath <<= fullClasspath in (compiler, Compile),
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
  .settings(ScriptedPlugin.scriptedSettings: _*)
  .settings(
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    // Publish definitions before running scripted
    scriptedDependencies <<= (
      publishLocal in definitions,
      publishLocal in runtime,
      scriptedDependencies) map { (_, _, _) =>
      Unit
    }
  )
  .enablePlugins(BuildInfoPlugin)

// Test coverage

lazy val coverageTests = (project in file("coverageTests"))
  .aggregate(server, client, runtime, definitions, compiler)

///////////////////
// Global settings:
///////////////////

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

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
  ";definitions/publishSigned;runtime/publishSigned;compiler/publishSigned;sbt-exercise/publishSigned")
