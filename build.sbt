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
  .aggregate(server, client, coreJs, coreJvm, runtime, definitions, compiler)
  .dependsOn(server, client, coreJs, coreJvm, runtime, definitions, compiler)

////////////////////
// Project Modules:
////////////////////

// Purely functional core

lazy val core = (crossProject(JSPlatform, JVMPlatform) in file("core"))
  .settings(
    libraryDependencies ++= Seq(
      %%("cats-core"),
    )
  ).jsSettings(sharedJsSettings: _*)

lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

// Client and Server projects
lazy val server = (project in file("server"))
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(coreJvm, `evaluator-client`)
  .enablePlugins(PlayScala)
  .enablePlugins(SbtWeb)
  .settings(noPublishSettings: _*)
  .settings(
    scalaJSProjects := clients,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(scalaJSProd, gzip),
    routesGenerator := InjectedRoutesGenerator,
    routesImport += "config.Routes._",
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.Specs2, "console")),
    libraryDependencies ++= Seq(
      filters,
      jdbc,
      evolutions,
      cacheApi,
      ws,
      caffeine,
      "org.scala-exercises" %% "runtime" % version.value changing (),
      // TODO: Just for testng
      //"org.scala-exercises" %% "exercises-stdlib" % v('stdlib) xscalaExercises,
      "org.postgresql"      % "postgresql"        % v('postgres),
      "com.vmunier"         %% "scalajs-scripts"  % v('scalajsscripts),
      "com.lihaoyi"         %% "upickle"          % v('upickle),
      "org.webjars"         %% "webjars-play"     % v('webjars),
      "org.webjars"             % "highlightjs"    % v('highlightjs),
      "org.foundweekends"       %% "knockoff"      % v('knockoff),
      "com.newrelic.agent.java" % "newrelic-agent" % v('newrelic),
      "commons-io"              % "commons-io"     % v('commonsio),
      "io.frees"                %% "frees-core"    % v('freestyle),
      "org.webjars.bower" % "bootstrap-sass" % v('bootstrap),
      %%("github4s"),
      %%("scalatest") % "runtime",
      %%("doobie-core"),
      %%("doobie-hikari"),
      %%("doobie-postgres"),
      %%("simulacrum"),
      specs2 xscalaz,
      %%("scheckShapeless") % "test",
      %%("doobie-specs2")   % "test"
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
    scalaJSMainModuleInitializer := Some(ModuleInitializer.mainMethod("org.scalaexercises.client.scripts.ExercisesJS", "main")),
    scalaJSUseMainModuleInitializer in Test := false,
    sourceMappings := SourceMappings.fromFiles(Seq(coreJs.base / "..")),
    scalaJSOptimizerOptions in (Compile, fullOptJS) ~= {
      _.withParallel(false)
    },
    jsDependencies += "org.webjars" % "jquery" % "3.4.1" / "3.4.1/jquery.js",
    skip in packageJSDependencies := false,
    jsEnv := new JSDOMNodeJSEnv(),
    //jsDependencies += RuntimeDOM % "test",
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "io.monix" %%% "monix" % v('monix),
      %%%("cats-core"),
      "com.lihaoyi" %%% "scalatags" % v('scalatags) xscalajs,
      "org.scala-js" %%% "scalajs-dom" % v('scalajsdom),
      "be.doeraene" %%% "scalajs-jquery" % v('scalajsjquery) xscalajs,
      "com.lihaoyi" %%% "upickle" % v('upickle),
      "com.lihaoyi" %%% "utest"   % v('utest) % "test"
    )
  )

lazy val `evaluator-client` = (project in file("eval-client"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "evaluator-client",
    libraryDependencies ++= Seq(
    %%("http4s-blaze-client", v('http4s)),
    %%("http4s-circe",  v('http4s)),
    %%("circe-core", v('circe)),
    %%("circe-generic", v('circe)),
    %%("circe-parser", v('circe)),
    %("slf4j-simple", v('slf4j)),
    %%("scalatest", v('scalatest)) % "test"
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
  .dependsOn(`evaluator-client`)
  .settings(name := "runtime")
  .settings(
    libraryDependencies ++= Seq(
      "org.clapper"         %% "classutil" % v('classutil),
      "io.monix"            %% "monix" % v('monix),
      %%("cats-core")       % "compile",
      %%("scalatest")       % "test"
    )
  )

// Compiler

lazy val compiler = (project in file("compiler"))
  .settings(name := "exercise-compiler")
  .settings(
    exportJars := true,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "compile",
      %%("github4s"),
      %%("cats-core") % "compile",
      %%("cats-laws") % "test",
      %%("scalatest") % "test"
    ),
    addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
  )
  .dependsOn(definitions, runtime)

// Compiler plugin

lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(name := "sbt-exercise")
  .settings(
    scalaVersion := "2.12.10",
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
  ";definitions/publishSigned;runtime/publishSigned;compiler/publishSigned;sbt-exercise/publishSigned")
