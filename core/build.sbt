import com.typesafe.sbt.SbtScalariform
// see common/BuildCommon.scala for all automatic settings

lazy val root = (project in file("."))
  .aggregate(compiler, definitions, runtime, `sbt-exercise`)

// Common settings
// TODO: consider moving this to BuildCommon
lazy val commonSettings = Seq(
  libraryDependencies ++= testlibs("org.scalatest" %% "scalatest" % "3.0.0-M15")
)

// ~ Exercise Definitions
lazy val definitions = (project in file("definitions"))
  .settings(
    name            := "definitions"
  )
  .settings(commonSettings: _*)

// ~ Exercise Compiler
lazy val compiler = (project in file("compiler"))
  .settings(
    name            := "exercise-compiler",
    exportJars      := true
  )
  .settings(commonSettings: _*)
  .settings(libraryDependencies <++= (scalaVersion)(scalaVersion =>
    compilelibs(
      "org.scala-lang" % "scala-compiler" % scalaVersion,
      "org.spire-math" %% "cats" % "0.3.0")
  ))
  .dependsOn(runtime % "test")

// ~ Exercise Runtime
lazy val runtime = (project in file("runtime"))
  .settings(
    name            := "runtime"
  )
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++=
    compilelibs(
      "org.spire-math" %% "cats" % "0.3.0"
    )
  )

// ~ Exercise SBT Plugin
lazy val `sbt-exercise` = (project in file("sbt-exercise"))
  .settings(
    name            := "sbt-exercise",
    sbtPlugin       := true
  )
  .settings(commonSettings: _*)
  // Leverage build info to populate compiler classpath--
  // This allows SBT, which currently requires Scala 2.10.x, to load and run
  // the compiler, which requires Scala 2.11.x.
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoObject   := "Meta",
    buildInfoPackage  := "com.fortysevendeg.exercises.sbt",
    buildInfoKeys     := Seq(
      BuildInfoKey.map(fullClasspath in (compiler, Compile)) {
        case (_, classFiles) ⇒ ("compilerClasspath", classFiles.map(_.data.toURI.toURL))
      }
    )
  )
  // scripted plugin
  .settings(ScriptedPlugin.scriptedSettings: _*)
  .settings(
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  // Make scalariform format the sources/exercises in scripted tests
  // ...because... well... why not?
  .settings(
    inConfig(scriptedConf)(SbtScalariform.configScalariformSettings),
    scripted <<= scripted dependsOn (scalariformFormat in scriptedConf),
    inConfig(scriptedConf) {
      sourceDirectories in scalariformFormat <<= sbtTestDirectory { dir =>
        val bases = dir * "*" * "*" / "src" / "main"
        (bases / "scala").get ++ (bases / "exercises").get
      }
    }
  )
