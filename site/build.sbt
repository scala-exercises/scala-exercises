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
  .dependsOn(
    sharedJvm,
    content)
  .dependsOn(ProjectRef(file("../core"), "runtime"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    routesGenerator := InjectedRoutesGenerator,
    routesImport += "config.Routes._",
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, gzip),
    herokuAppName in Compile := "scala-exercises"
  )
  .settings(libraryDependencies <++= (scalaVersion)(scalaVersion =>
    compilelibs(
      filters,
      jdbc,
      evolutions,
      cache,
      ws,
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "com.vmunier" %% "play-scalajs-scripts" % "0.2.1",
      "com.lihaoyi" %% "upickle" % "0.2.8",
      "org.webjars" %% "webjars-play" % "2.3.0",
      "org.webjars" % "bootstrap-sass" % "3.2.0",
      "org.webjars" % "highlightjs" % "8.7",
      "org.webjars.npm" % "highlight.js" % "9.1.0",
      "com.tristanhunt" %% "knockoff" % "0.8.3",
      "org.scala-lang" % "scala-compiler" % scalaVersion,
      "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion exclude("org.scalaz", "scalaz-concurrent"),
      "org.tpolecat" %% "doobie-contrib-hikari" % doobieVersion exclude("org.scalaz", "scalaz-concurrent"),
      "org.tpolecat" %% "doobie-contrib-postgresql" % doobieVersion exclude("org.scalaz", "scalaz-concurrent")) ++
    testlibs(
      specs2,
      "org.typelevel" %% "scalaz-specs2" % "0.3.0",
      "org.scalacheck" %% "scalacheck" % "1.12.5",
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % "0.3.1",
      "org.tpolecat" %% "doobie-contrib-specs2" % doobieVersion) :+
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
  ))


lazy val client = (project in file("client"))
  .dependsOn(sharedJs)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(commonSettings: _*)
  .settings(
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
  .jsSettings(sourceMapsBase := baseDirectory.value / "..")
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++=
    compilelibs(
      "org.scalatest" %% "scalatest" % "2.2.4",
      "org.typelevel" %%% "cats-core" % "0.4.1"
    )
  )

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// Locally bundled exercise content projects

import de.heikoseeberger.sbtheader.HeaderPlugin

lazy val content = (project in file("content"))
  .enablePlugins(ExerciseCompilerPlugin)
  .dependsOn(ProjectRef(file("../core"), "runtime"))
  .dependsOn(ProjectRef(file("../core"), "runtime") % CompileGeneratedExercises)
  .dependsOn(ProjectRef(file("../core"), "definitions"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++=
    compilelibs(
      "org.scalatest" %% "scalatest" % "2.2.4",
      "com.chuusai" %% "shapeless" % "2.2.5"
    ) ++
    testlibs(
      "org.scalatest" %% "scalatest" % "2.2.4",
      "org.scalaz" %% "scalaz-core" % scalazVersion,
      "org.scalacheck" %% "scalacheck" % "1.12.5",
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % "0.3.1"
    ) :+
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
  )
