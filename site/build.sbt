import play.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef
import NativePackagerHelper._


// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

// Common settings

lazy val commonSettings = Seq(
  resolvers ++= Seq(
    "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
    Resolver.sonatypeRepo("snapshots")
  )
)

// Client and Server projects

lazy val clients = Seq(client)
lazy val server = (project in file("server"))
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(
    sharedJvm,
    content)
  .dependsOn(ProjectRef(file("../core"), "runtime"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    routesImport += "config.Routes._",
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, gzip))
  .settings(libraryDependencies <++= (scalaVersion)(scalaVersion =>
    compilelibs(
      filters,
      jdbc,
      evolutions,
      cache,
      ws,
      "com.typesafe.slick" %% "slick" % "3.0.0-RC1",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "com.vmunier" %% "play-scalajs-scripts" % "0.2.1",
      "com.lihaoyi" %% "upickle" % "0.2.8",
      "org.webjars" %% "webjars-play" % "2.3.0",
      "org.webjars" % "bootstrap-sass" % "3.2.0",
      "org.webjars" % "jquery" % "2.1.1",
      "org.webjars" % "font-awesome" % "4.1.0",
      "org.webjars" % "highlightjs" % "8.7",
      "com.tristanhunt" %% "knockoff" % "0.8.3",
      "org.scala-lang" % "scala-compiler" % scalaVersion,
      "org.clapper" %% "classutil" % "1.0.5",
      "com.toddfast.typeconverter" % "typeconverter" % "1.0") ++
    testlibs(
      specs2,
      "org.typelevel" %% "scalaz-specs2" % "0.3.0")
  ))


lazy val client = (project in file("client"))
  .dependsOn(sharedJs)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(commonSettings: _*)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    sourceMapsDirectories += sharedJs.base / "..",
    jsDependencies += RuntimeDOM,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.1",
      "com.lihaoyi" %%% "scalatags" % "0.5.2",
      "com.lihaoyi" %%% "scalarx" % "0.2.8",
      "be.doeraene" %%% "scalajs-jquery" % "0.8.0",
      "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
      "com.lihaoyi" %%% "upickle" % "0.2.8",
      "org.spire-math" %%% "cats-core" % "0.4.0-SNAPSHOT" changing()
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
      "org.spire-math" %%% "cats-core" % "0.4.0-SNAPSHOT" changing()
    )
  )

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// Locally bundled exercise content projects

lazy val content = (project in file("content"))
  .enablePlugins(ExerciseCompilerPlugin)
  .dependsOn(ProjectRef(file("../core"), "runtime"))
  .dependsOn(ProjectRef(file("../core"), "definitions") % "compile-exercises-source")
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++=
    compilelibs(
      "org.scalaz" %% "scalaz-core" % "7.1.4",
      "org.scalatest" %% "scalatest" % "2.2.4"
    )
  )
