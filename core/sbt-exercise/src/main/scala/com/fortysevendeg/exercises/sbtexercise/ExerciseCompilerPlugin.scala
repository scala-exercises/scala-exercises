
package com.fortysevendeg.exercises
package sbtexercise

import sbt.{ `package` ⇒ _, _ }
import sbt.Keys._
import sbt.classpath.ClasspathUtilities
import xsbt.api.Discovery

import scala.io.Codec
import scala.util.{ Try, Success, Failure }

import java.io.File
import java.net.URLClassLoader

import cats._
import cats.data.Xor
import cats.syntax.flatMap._

object ExerciseCompilerPlugin extends AutoPlugin {
  import Def.Initialize

  override def requires = plugins.JvmPlugin
  override def trigger = noTrigger

  val CompileMain = config("compile-main")

  val CompileExercisesSource = config("compile-exercises-source") extend CompileMain

  val CompileExercises = config("compile")

  type PreGenEx = Seq[String]
  val preGenEx = TaskKey[PreGenEx]("pregen-exercises")

  object autoImport {
    def CompileExercises = ExerciseCompilerPlugin.CompileExercises
  }

  /** Given an Analysis output from a compile run, this will
    * identify all modules implementing `exercise.Library`.
    */
  def discoverLibraries(analysis: inc.Analysis): Seq[String] =
    Discovery(Set("exercise.Library"), Set.empty)(Tests.allDefs(analysis))
      .collect({
        case (definition, discovered) if !discovered.isEmpty ⇒ definition.name
      }).sorted

  def preGenExTask = Def.task {
    val log = streams.value.log
    log.warn("doing pregen task!")
    lazy val analysisIn = (compile in CompileExercisesSource).value

    val libraryNames = discoverLibraries(analysisIn)

    val nativeTmp = taskTemporaryDirectory.value
    val instance = scalaInstance.value
    val classpath = Attributed.data((fullClasspath in CompileExercisesSource).value)
    val loader = ClasspathUtilities.makeLoader(classpath, instance, nativeTmp)

    def catching[A](f: ⇒ A)(msg: ⇒ String) =
      Xor.catchNonFatal(f).leftMap(_ ⇒ msg)

    val foo = libraryNames.map { name ⇒

      val module = for {
        loadedClass ← catching(Class.forName(name, true, loader))(s"${name} not found")
        loadedModule ← catching(loadedClass.getField("MODULE$").get(null))(s"${name} must be defined as an object")
      } yield loadedModule

      println("MODULE " + module)
    }

    println(foo)
    log.warn("FOO " + foo)


    libraryNames
  }

  def generateExerciseSourcesTask = Def.task {
    val log = streams.value.log

    lazy val preGen = preGenEx.value

    log.warn("Pregen info!")
    log.warn("> " + preGen)

    val dir = (sourceManaged in CompileExercises).value

    val file = dir / "demo" / "Test.scala"
    IO.write(file, """
      import com.fortysevendeg.exercises._
      package foo {
      object libFoo extends Library {
        def name: String = "Foo"
        def description: String = "This is my silly library"
        def color: String = "blue"
        def sections: List[Section] =
          DefaultSection(
            name = "Section 1",
            description = Some("Description of Section 1"),
            exercises = DefaultExercise[Unit](
              name = Some("Exercise 1"),
              description = Some("Description of Exercies 1"),
              code = Some("object HelloWorld extends App {\n  println(\"hello world!\")\n}"),
              explanation = Some("Go away!")
            ) :: Nil
          ) ::
          DefaultSection(
            name = "Section 2",
            description = Some("Description of Section 2"),
            exercises = Nil
          ) ::
          DefaultSection(
            name = "Section 3",
            description = Some("Description of Section 3"),
            exercises = Nil
          ) ::
          Nil
      }
      object libBar extends Library {
        def name: String = "Bar"
        def description: String = "Hello, world?"
        def color: String = "cyan"
        def sections: List[Section] =  Nil
      }
      }
      """)
    Seq(file)
  }

  def generateExerciseDescriptorTask = Def.task {
    val log = streams.value.log

    lazy val preGen = preGenEx.value

    log.warn("Pregen info!")
    log.warn("> " + preGen)

    val qualifiedLibraryInstancies =
      "foo.libFoo$" :: "foo.libBar$" :: Nil

    val dir = (resourceManaged in CompileExercises).value
    val resourceFile = dir / "scala-exercises" / "library.47"
    IO.write(resourceFile, qualifiedLibraryInstancies.mkString("\n"))

    Seq(resourceFile)
  }

  // format: OFF
  override def projectSettings =
    inConfig(CompileMain)(
      Defaults.compileSettings ++
      Defaults.compileInputsSettings ++
      Defaults.configTasks ++
      redirSettings
    ) ++
    inConfig(CompileExercisesSource)(
      // this configuration compiles code in src/main/exercises
      Defaults.compileSettings ++
      Defaults.compileInputsSettings ++
      redirSettings ++
      Seq(
        // adjusting source directory to be src/main/exercises
        scalaSource := sourceDirectory.value / "exercises")
    ) ++
    inConfig(CompileExercises)(
      // this configuration compiles source code we generate in CompileExercisesSource
      Defaults.compileSettings ++
      Defaults.compileInputsSettings ++
      Defaults.configTasks ++ Seq(

      fork := false,

      // disable any user defined source files for this scope as
      // we only want to compile the generated files
      unmanagedSourceDirectories := Nil,
      sourceGenerators <+= generateExerciseSourcesTask,
      resourceGenerators <+= generateExerciseDescriptorTask,

      preGenEx <<= preGenExTask
    )) ++
    inConfig(Compile)(
      // All your base are belong to us!! (take over standard compile)
      classpathConfiguration := CompileExercises
    ) ++
    Seq(
      defaultConfiguration := Some(CompileMain),

      // library dependences have to be declared at the root level
      ivyConfigurations   := overrideConfigs(CompileMain, CompileExercisesSource, CompileExercises)(ivyConfigurations.value),
      libraryDependencies += "com.47deg" %% "definitions" % Meta.version % CompileExercisesSource.name,
      libraryDependencies += "com.47deg" %% "runtime" % Meta.version % CompileExercises.name
    )
  // format: ON

  def redirSettings = Seq(
    // v0.13.9/main/src/main/scala/sbt/Defaults.scala
    sourceDirectory <<= reconfigureSub(sourceDirectory),
    sourceManaged <<= reconfigureSub(sourceManaged),
    resourceManaged <<= reconfigureSub(resourceManaged)
  )

  /** Helper to faciliate changing the directories. By default, a configuration
    * inheriting from Compile will compile source in
    * `src/<configuration_name>/[scala|test|...]`. This forces the directory
    * back to `src/main/[scala|test|...]`.
    */
  private def reconfigureSub(key: SettingKey[File]): Initialize[File] =
    (key in ThisScope.copy(config = Global), configuration) { (src, conf) ⇒ src / "main" }

}
