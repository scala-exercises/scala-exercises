import play.sbt.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef
import webscalajs._

////////////////////
// Project Modules:
////////////////////

// Purely functional core

lazy val core = (crossProject in file("core"))
  .settings(
    libraryDependencies ++= Seq(
      %%("cats-core"),
      %%("cats-free"),
      "com.47deg" %% "freestyle" % v('freestyle)
    )
  )
  .jsSettings(sharedJsSettings: _*)

lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

// Client and Server projects

lazy val server = (project in file("server"))
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(coreJvm)
  .enablePlugins(PlayScala)
  .settings(noPublishSettings: _*)
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
      "org.scala-exercises"     %% "exercises-stdlib"        % v('stdlib),
      "org.scala-exercises"     %% "exercises-cats"          % v('cats),
      "org.scala-exercises"     %% "exercises-shapeless"     % v('shapeless),
      "org.scala-exercises"     %% "exercises-doobie"        % v('doobie),
      "org.scala-exercises"     %% "exercises-scalacheck"    % v('scalacheck),
      "org.scala-exercises"     %% "exercises-fpinscala"     % v('fpinscala),
      "org.scala-exercises"     %% "exercises-scalatutorial" % v('scalatutorial),
      "org.scala-exercises"     %% "evaluator-client"        % v('evaluator) changing (),
      "org.scala-exercises"     %% "runtime"                 % version.value changing (),
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
      %%("scalazspecs2")    % "test"
    )
  )

lazy val client = (project in file("client"))
  .dependsOn(coreJs)
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .settings(name := "client")
  .settings(noPublishSettings: _*)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
    sourceMappings := SourceMappings.fromFiles(Seq(coreJs.base / "..")),
    scalaJSOptimizerOptions in (Compile, fullOptJS) ~= {
      _.withParallel(false)
    },
    jsDependencies ++= Seq(
      "org.webjars" % "jquery"    % v('jquery) / s"${v('jquery)}/jquery.js" minified "jquery.min.js",
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
      "com.lihaoyi"   %%% "utest"                    % v('utest) % "test",
      "com.lihaoyi"   %%% "upickle"                  % v('upickle),
      "org.scoverage" %%% "scalac-scoverage-runtime" % v('scalacscoverage)
    )
  )

lazy val clients = Seq(client)

// Definitions

lazy val definitions = (project in file("definitions"))
  .settings(name := "definitions")
  .settings(
    libraryDependencies ++= Seq(
      %%("cats-core"),
      %%("scalatest"),
      %%("scalacheck"),
      %%("scheckShapeless")
    )
  )

// Runtime

lazy val runtime = (project in file("runtime"))
  .settings(name := "runtime")
  .settings(
    libraryDependencies ++= Seq(
      "org.clapper"         %% "classutil"        % v('classutil),
      "com.twitter"         %% "util-eval"        % v('utileval),
      "org.scala-exercises" %% "evaluator-shared" % v('evaluator) changing (),
      %%("monix"),
      %%("cats-core") % "compile",
      %%("scalatest") % "test"
    )
  )

// Compiler

lazy val compiler = (project in file("compiler"))
  .settings(name := "exercise-compiler")
  .settings(
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
  .settings(name := "sbt-exercise")
  .settings(
    scalaVersion := "2.10.6",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      %%("cats-core") % "compile"
    ),
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
  .settings(ScriptedPlugin.scriptedSettings: _*)
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

// Test coverage

lazy val coverageTests = (project in file("coverageTests"))
  .settings(noPublishSettings: _*)
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
