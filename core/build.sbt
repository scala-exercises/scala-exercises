lazy val runtime = (project in file("runtime"))
  .settings(
    name            := "runtime"
  )
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

lazy val root = (project in file("."))
  .aggregate(runtime, `sbt-exercise`)
