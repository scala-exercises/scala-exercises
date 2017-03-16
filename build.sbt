import play.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef


////////////////////
// Project Modules:
////////////////////

// Purely functional core

lazy val core = (crossProject.crossType(CrossType.Pure) in file("core"))
  .jsConfigure(_ enablePlugins ScalaJSPlay)
  .jsSettings(
    sourceMapsBase := baseDirectory.value / "..",
    scalaVersion := v('scala)
  )
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "com.fortysevendeg" %%% "freestyle" % v('freestyle),
      compilerPlugin("org.spire-math" %% "kind-projector" % v('kindprojector)),
      compilerPlugin("org.scalamacros" % "paradise" % v('paradise) cross CrossVersion.full)
    )
  )

lazy val coreJvm = core.jvm
lazy val coreJs = core.js

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
      "com.fortysevendeg" %% "freestyle" % v('freestyle),
      "org.scala-exercises" %% "exercises-stdlib" % version.value changing(),
      "org.scala-exercises" %% "exercises-cats" % version.value changing(),
      "org.scala-exercises" %% "exercises-shapeless" % version.value changing(),
      "org.scala-exercises" %% "exercises-doobie" % version.value changing(),
      "org.scala-exercises" %% "exercises-scalacheck" % version.value changing(),
      "org.scala-exercises" %% "exercises-fpinscala" % version.value changing(),
      "org.scala-exercises" %% "exercises-scalatutorial" % version.value changing(),
      "org.scala-exercises" %% "runtime" % version.value changing(),
      "org.scala-exercises" %% "evaluator-client" % v('evaluator) changing(),
      "org.slf4j" % "slf4j-nop" % v('slf4j),
      "org.postgresql" % "postgresql" % v('postgres),
      "com.vmunier" %% "play-scalajs-scripts" % v('scalajsscripts),
      "com.lihaoyi" %% "upickle" % v('upickle),
      "org.webjars" %% "webjars-play" % v('webjars),
      "org.webjars" % "bootstrap-sass" % v('bootstrap),
      "org.webjars" % "highlightjs" % v('highlightjs),
      "com.tristanhunt" %% "knockoff" % v('knockoff),
      "com.fortysevendeg" %% "github4s" % v('github4s),
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scalaz" %% "scalaz-concurrent" % v('scalaz),
      "org.scalatest" %% "scalatest" % v('scalaTest) % "runtime",
      "com.newrelic.agent.java" % "newrelic-agent" % v('newrelic),
      "org.tpolecat" %% "doobie-core" % v('doobie) xscalaz,
      "org.tpolecat" %% "doobie-contrib-hikari" % v('doobie) xscalaz,
      "org.tpolecat" %% "doobie-contrib-postgresql" % v('doobie) xscalaz,
      "com.github.mpilquist" %% "simulacrum" % v('simulacrum),
      "commons-io" % "commons-io" % v('commonsio),
      specs2 xscalaz,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % v('scalacheckshapeless) % "test",
      "org.tpolecat" %% "doobie-contrib-specs2" % v('doobie) % "test",
      "org.typelevel" %% "scalaz-specs2" % v('scalazspecs2) % "test",
      compilerPlugin("org.spire-math" %% "kind-projector" % v('kindprojector)),
      compilerPlugin("org.scalamacros" % "paradise" % v('paradise) cross CrossVersion.full)))

lazy val client = (project in file("client"))
  .dependsOn(core.js)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    sourceMapsDirectories += core.js.base / "..",
    scalaJSOptimizerOptions in(Compile, fullOptJS) ~= {
      _.withParallel(false)
    },
    jsDependencies ++= Seq(
      "org.webjars" % "bootstrap" % v('bootstrap) / "bootstrap.js" minified "bootstrap.min.js"
    ),
    jsDependencies += RuntimeDOM % "test",
    skip in packageJSDependencies := false,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % v('scalajsdom),
      "com.lihaoyi" %%% "scalatags" % v('scalatags) xscalajs,
      "io.monix" %%% "monix" % "2.2.3",
      "io.monix" %%% "monix-cats" % "2.2.3",
      "be.doeraene" %%% "scalajs-jquery" % v('scalajsjquery) xscalajs,
      "com.lihaoyi" %%% "utest" % v('utest) % "test",
      "com.lihaoyi" %%% "upickle" % v('upickle),
      "org.typelevel" %%% "cats-core" % v('cats),
      "ch.sidorenko.scoverage" %%% "scalac-scoverage-runtime" % v('scalacscoverage)
    )
  )

lazy val clients = Seq(client)

// Definitions

lazy val definitions = (project in file("definitions"))
  .settings(
    name := "definitions",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % v('cats),
      "org.scalatest" %% "scalatest" % v('scalaTest),
      "org.scalacheck" %% "scalacheck" % v('scalacheck),
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % v('scalacheckshapeless)
    )
  )

// Runtime

lazy val runtime = (project in file("runtime"))
  .settings(
    name := "runtime",
    libraryDependencies ++= Seq(
      "org.clapper" %% "classutil" % v('classutil),
      "com.twitter" %% "util-eval" % v('utileval),
      "io.monix" %% "monix" % v('monix),
      "org.scala-exercises" %% "evaluator-shared" % v('evaluator) changing(),
      "org.typelevel" %%% "cats-core" % v('cats) % "compile",
      "org.scalatest" %% "scalatest" % v('scalaTest) % "test"
    )
  )

// Compiler

lazy val compiler = (project in file("compiler"))
  .settings(
    name := "exercise-compiler",
    exportJars := true,
    libraryDependencies ++= Seq(
      "org.scalariform" %% "scalariform" % v('scalariform),
      "com.fortysevendeg" %% "github4s" % v('github4s),
      "org.typelevel" %% "cats-core" % v('cats) % "compile",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "compile",
      "org.typelevel" %% "cats-laws" % v('cats) % "test",
      "org.scalatest" %% "scalatest" % v('scalaTest) % "test"
    )
  ).dependsOn(definitions, runtime)

// Compiler plugin

lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(
    name := "sbt-exercise",
    scalaVersion := "2.10.6",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % v('cats) % "compile"
    ),
    // Leverage build info to populate compiler classpath--
    // This allows SBT, which currently requires Scala 2.10.x, to load and run
    // the compiler, which requires Scala 2.11.x.
    compilerClasspath <<= fullClasspath in(compiler, Compile),
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
    scriptedDependencies <<= (publishLocal in definitions, publishLocal in runtime, scriptedDependencies) map { (_, _, _) => Unit }
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


// These settings should be global to be able to capture the env var:
lazy val gpgFolder = sys.env.getOrElse("PGP_FOLDER", ".")

pgpPassphrase := Some(sys.env.getOrElse("PGP_PASSPHRASE", "").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.gpg")
pgpSecretRing := file(s"$gpgFolder/secring.gpg")

lazy val compilerClasspath = TaskKey[Classpath]("compiler-classpath")

// Aliases

addCommandAlias("testAll", ";server/test;client/test;definitions/test;runtime/test;compiler/test;sbt-exercise/scripted")
addCommandAlias("publishAll", ";definitions/publishLocal;runtime/publishLocal;compiler/publishLocal;sbt-exercise/publishLocal")
addCommandAlias("publishSignedAll", ";definitions/publishSigned;runtime/publishSigned;compiler/publishSigned;sbt-exercise/publishSigned")

