import play.PlayImport._
import play.PlayImport.PlayKeys._
import sbt.Project.projectToRef

lazy val clients = Seq(scalaExercisesClient)
lazy val scalaV = "2.11.7"

lazy val scalaExercisesServer = (project in file("scala-exercises-server")).settings(
  scalaVersion := scalaV,
  routesImport += "config.Routes._",
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd, gzip),
  resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= Seq(
    filters,
    jdbc,
    evolutions,
    cache,
    ws,
    specs2 % Test,
    "com.typesafe.slick" %% "slick" % "3.0.0-RC1",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
    "com.vmunier" %% "play-scalajs-scripts" % "0.2.1",
    "com.lihaoyi" %% "upickle" % "0.2.8",
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "bootstrap-sass" % "3.2.0",
    "org.webjars" % "jquery" % "2.1.1",
    "org.webjars" % "font-awesome" % "4.1.0",
    "com.tristanhunt" %% "knockoff" % "0.8.3",
    "org.scala-lang" % "scala-compiler" % scalaV,
    "org.clapper" %% "classutil" % "1.0.5",
    "com.toddfast.typeconverter" % "typeconverter" % "1.0",
    "org.typelevel" %% "scalaz-specs2" % "0.3.0" % "test"
  )

).enablePlugins(PlayScala).
    aggregate(clients.map(projectToRef): _*).
    dependsOn(scalaExercisesSharedJvm, scalaExercisesContent)

lazy val scalaExercisesClient = (project in file("scala-exercises-client")).settings(
  scalaVersion := scalaV,
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
    "com.lihaoyi" %%% "upickle" % "0.2.8"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
    dependsOn(scalaExercisesSharedJs)

lazy val scalaExercisesShared = (crossProject.crossType(CrossType.Pure) in file("scala-exercises-shared")).
    settings(
      scalaVersion := scalaV,
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.4",
        "org.scalaz" %% "scalaz-core" % "7.1.4"
      )
    ).
    jsConfigure(_ enablePlugins ScalaJSPlay).
    jsSettings(sourceMapsBase := baseDirectory.value / "..")

lazy val scalaExercisesSharedJvm = scalaExercisesShared.jvm
lazy val scalaExercisesSharedJs = scalaExercisesShared.js

lazy val scalaExercisesContent = (project in file("scala-exercises-content")).
    settings(
      scalaVersion := scalaV,
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.4",
        "org.scalaz" %% "scalaz-core" % "7.1.4"
      ),
      unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "scala"
    ).dependsOn(scalaExercisesSharedJvm)

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project scalaExercisesServer", _: State)) compose (onLoad in Global).value