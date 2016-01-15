// see common/BuildCommon.scala for all automatic settings

lazy val compiler = (project in file("compiler"))
  .settings(
    name            := "exercise-compiler"
  )
  .dependsOn(runtime % "test")
  .settings(testSettings: _*)
  .settings(libraryDependencies <++= (scalaVersion)(scalaVersion =>
    compilelibs(
      "org.scala-lang" % "scala-compiler" % scalaVersion,
      "org.spire-math" %% "cats" % "0.3.0") ++
    testlibs(
      "com.github.nikita-volkov" % "sext" % "0.2.4"
    )
  ))

lazy val definitions = (project in file("definitions"))
  .settings(
    name            := "definitions"
  )
  .settings(testSettings: _*)

lazy val runtime = (project in file("runtime"))
  .settings(
    name            := "runtime"
  )
  .settings(testSettings: _*)
  .settings(libraryDependencies ++=
    compilelibs(
      "org.spire-math" %% "cats" % "0.3.0"
    )
  )

lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(
    name            := "sbt-exercise",
    sbtPlugin       := true
  )
  .settings(testSettings: _*)

lazy val root = (project in file("."))
  .aggregate(compiler, definitions, runtime, `sbt-exercise`)

lazy val testSettings = Seq(
  libraryDependencies ++= testlibs("org.scalatest" %% "scalatest" % "3.0.0-M15")
)
