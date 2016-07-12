val pluginVersion = System.getProperty("plugin.version")

lazy val content = (project in file("content"))
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "org.scala-exercises" %% "runtime" % pluginVersion changing(),
      "org.scala-exercises" %% "exercise-compiler" % pluginVersion changing(),
      "org.scala-exercises" %% "definitions" % pluginVersion changing()
    )
)

lazy val contentInPackages = (project in file("contentinpackages"))
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "org.scala-exercises" %% "runtime" % pluginVersion changing(),
      "org.scala-exercises" %% "exercise-compiler" % pluginVersion changing(),
      "org.scala-exercises" %% "definitions" % pluginVersion changing()
    )
  )

lazy val check = (project in file("check"))
  .dependsOn(content, contentInPackages)
  .settings(
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
)
