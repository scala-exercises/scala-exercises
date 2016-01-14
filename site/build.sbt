import play.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef
import NativePackagerHelper._


// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project scalaExercisesServer", _: State)) compose (onLoad in Global).value

// Common settings

lazy val commonSettings = Seq(
  wartremoverWarnings in Compile ++= Warts.unsafe,
  resolvers ++= Seq(
    "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
    Resolver.sonatypeRepo("snapshots")
  )
)

// Client and Server projects

lazy val Exercise = config("exercise") extend(Runtime) describedAs("Exercise dependencies.")

lazy val exerciseSettings: Seq[Setting[_]] =
  inConfig(Exercise)(Defaults.configSettings) ++ Seq(
    ivyConfigurations += Exercise,
    classpathConfiguration in Runtime := Exercise
  )

lazy val clients = Seq(scalaExercisesClient)
lazy val scalaExercisesServer = (project in file("scala-exercises-server"))
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(
    scalaExercisesSharedJvm,
    scalaExerciseV0Definitions,
    scalaExercisesContent % "exercise")
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(exerciseSettings: _*)
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


lazy val scalaExercisesClient = (project in file("scala-exercises-client"))
  .dependsOn(scalaExercisesSharedJs)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(commonSettings: _*)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    sourceMapsDirectories += scalaExercisesSharedJs.base / "..",
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

lazy val scalaExercisesShared = (crossProject.crossType(CrossType.Pure) in file("scala-exercises-shared"))
  .jsConfigure(_ enablePlugins ScalaJSPlay)
  .jsSettings(sourceMapsBase := baseDirectory.value / "..")
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++=
    compilelibs(
      "org.scalatest" %% "scalatest" % "2.2.4",
      "org.spire-math" %%% "cats-core" % "0.4.0-SNAPSHOT" changing()
    )
  )

lazy val scalaExercisesSharedJvm = scalaExercisesShared.jvm
lazy val scalaExercisesSharedJs = scalaExercisesShared.js


// V0 (*insert-better-name-here-pls*) style exercise definition traits etc.
lazy val scalaExerciseV0Definitions = (project in file("scala-exercises-v0def"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++=
    compilelibs(
      "org.scalaz" %% "scalaz-core" % "7.1.4"
    )
  )

// Locally bundled exercise content projects


lazy val scalaExercisesContent = (project in file("scala-exercises-content"))
  .dependsOn(scalaExercisesSharedJvm, scalaExerciseV0Definitions)
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(commonSettings: _*)
  .settings(
    // TODO: leverage a plugin instead of this trick
    unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "main" / "exercises",
    unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "exercises")
  .settings(libraryDependencies ++=
    compilelibs(
      "org.scalatest" %% "scalatest" % "2.2.4",
      "org.scalaz" %% "scalaz-core" % "7.1.4"
    )
  )
