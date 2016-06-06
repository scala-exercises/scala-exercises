lazy val definitions =  RootProject(uri("git://github.com/scala-exercises/definitions.git"))

lazy val content = (project in file("content"))
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(
    scalaVersion := "2.11.7"
  ).dependsOn(definitions)

lazy val check = (project in file("check"))
  .dependsOn(content)
  .settings(scalaVersion := "2.11.7")
