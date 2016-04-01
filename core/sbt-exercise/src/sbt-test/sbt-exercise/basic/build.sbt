lazy val content = (project in file("content"))
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(scalaVersion := "2.11.7")

lazy val check = (project in file("check"))
  .dependsOn(content)
  .settings(scalaVersion := "2.11.7")
