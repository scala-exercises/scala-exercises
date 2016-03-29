lazy val root = (project in file("."))
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(scalaVersion := "2.11.7")
