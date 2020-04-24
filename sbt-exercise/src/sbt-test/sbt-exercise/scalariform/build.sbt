val pluginVersion = System.getProperty("plugin.version")

lazy val root = (project in file("."))
  .settings(
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.defaultLocal
    ),
    libraryDependencies ++= Seq(
      "org.scala-exercises" %% "exercise-compiler" % pluginVersion changing ()
    )
  )
  .enablePlugins(ExerciseCompilerPlugin)
