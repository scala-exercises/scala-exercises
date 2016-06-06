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
    Resolver.mavenLocal,
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
    herokuAppName in Compile := "scala-exercises",
  libraryDependencies ++= Seq(
      filters,
      jdbc,
      evolutions,
      cache,
    ws,
      "org.scalaexercises" %% "runtime" % "0.0.0-SNAPSHOT" changing(),
      "org.scalaexercises" %% "content" % "0.0.0-SNAPSHOT" % "runtime",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "com.vmunier" %% "play-scalajs-scripts" % "0.2.1",
      "com.lihaoyi" %% "upickle" % "0.2.8",
      "org.webjars" %% "webjars-play" % "2.3.0",
      "org.webjars" % "bootstrap-sass" % "3.2.0",
      "org.webjars" % "highlightjs" % "8.7",
      "org.webjars.npm" % "highlight.js" % "9.1.0",
      "com.tristanhunt" %% "knockoff" % "0.8.3",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
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

      "org.scalatest" %% "scalatest" % "2.2.4" % "compile",
      "org.typelevel" %%% "cats-core" % "0.4.1" % "compile"

  )

  )

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// Definitions

lazy val definitions = (project in file("definitions"))
  .settings(
    organization := "org.scalaexercises",
    version := "0.0.0-SNAPSHOT",
    name := "definitions",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.0-M15"
    )
)

// Runtime evaluation

lazy val runtime = (project in file("runtime"))
  .settings(
    organization := "org.scalaexercises",
    version := "0.0.0-SNAPSHOT",
    name := "runtime",
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "compile",
      "org.clapper" %% "classutil" % "1.0.11",
      "org.typelevel" %% "cats-core" % "0.4.1" % "compile",
      "org.scalatest" %% "scalatest" % "3.0.0-M15" % "test"
    )
)

// Compiler & Compiler plugin

lazy val compiler = (project in file("compiler"))
  .settings(
    organization := "org.scalaexercises",
    name := "exercise-compiler",
    version := "0.0.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    exportJars      := true,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "org.scalariform" %% "scalariform" % "0.1.8",
      "org.scalaexercises" %% "runtime" % "0.0.0-SNAPSHOT" changing(),
      "org.scalaexercises" %% "definitions" % "0.0.0-SNAPSHOT" changing(),
      "org.typelevel" %% "cats-core" % "0.4.1" % "compile",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "compile",
      "org.typelevel" %% "cats-laws" % "0.4.1" % "test"
    )
 )

lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(
    organization := "org.scalaexercises",
    name            := "sbt-exercise",
    version := "0.0.0-SNAPSHOT",
    scalaVersion := "2.10.6",
    sbtPlugin       := true,
    resolvers ++= Seq(
      Resolver.mavenLocal,
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

lazy val compilerClasspath = TaskKey[Classpath]("compiler-classpath")

// Aliases

addCommandAlias("publishAll", ";definitions/publishLocal;runtime/publishLocal;compiler/publishLocal;sbt-exercise/publishLocal")

