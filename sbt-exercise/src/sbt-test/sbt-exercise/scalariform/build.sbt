val pluginVersion = System.getProperty("plugin.version")

lazy val root = (project in file("."))
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "org.scala-exercises" %% "exercise-compiler" % pluginVersion changing()
    )
)
