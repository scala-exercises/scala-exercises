
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
import cats.std.all._
import cats.syntax.flatMap._
import cats.syntax.traverse._

object ExerciseCompilerPlugin extends AutoPlugin {
  import Def.Initialize

  override def requires = plugins.JvmPlugin
  override def trigger = noTrigger

  val CompileMain = config("compile-main")

  val CompileExercisesSource = config("compile-exercises-source") extend CompileMain

  val CompileExercises = config("compile")

  type PreGenEx = List[(String, String)]
  val preGenEx = TaskKey[PreGenEx]("pregen-exercises")

  object autoImport {
    def CompileExercises = ExerciseCompilerPlugin.CompileExercises
  }

  /** Given an Analysis output from a compile run, this will
    * identify all modules implementing `exercise.Library`.
    */
  private def discoverLibraries(analysis: inc.Analysis): Seq[String] =
    Discovery(Set("exercise.Library"), Set.empty)(Tests.allDefs(analysis))
      .collect({
        case (definition, discovered) if !discovered.isEmpty ⇒ definition.name
      }).sorted

  private def discoverSections(analysis: inc.Analysis): Seq[String] =
    Discovery(Set("exercise.Section"), Set.empty)(Tests.allDefs(analysis))
      .collect({
        case (definition, discovered) if !discovered.isEmpty ⇒ definition.name
      }).sorted

  private def catching[A](f: ⇒ A)(msg: ⇒ String) =
    Xor.catchNonFatal(f).leftMap(_ ⇒ msg)

  private val COMPILER_CLASS = "com.fortysevendeg.exercises.compiler.CompilerJava"
  private type COMPILER = {
    def compile(library: AnyRef, sources: Array[String], targetPackage: String): Array[String]
  }

  def preGenExTask = Def.task {
    val log = streams.value.log
    log.info("Compiling exercises")
    lazy val analysisIn = (compile in CompileExercisesSource).value

    lazy val libraryNames = discoverLibraries(analysisIn)
    lazy val sectionNames = discoverSections(analysisIn)

    val nativeTmp = taskTemporaryDirectory.value
    val libraryClasspath = Attributed.data((fullClasspath in CompileExercisesSource).value)
    val libraryLoader = ClasspathUtilities.makeLoader(libraryClasspath, scalaInstance.value, nativeTmp)

    val compilerLoader = ClasspathUtilities.toLoader(
      Meta.compilerClasspath,
      libraryLoader,
      ClasspathUtilities.createClasspathResources(
        appPaths = Meta.compilerClasspath,
        bootPaths = Nil
      )
    )

    // this desperately needs to be cleaned up!
    val result = for {
      compilerClass ← catching(compilerLoader.loadClass(COMPILER_CLASS))("Unable to find exercise compiler class")
      compiler ← catching(compilerClass.newInstance.asInstanceOf[COMPILER])("Unable to create instance of exercise compiler")
      libraries ← libraryNames
        .map { name ⇒
          for {
            loadedClass ← catching(Class.forName(name + "$", true, libraryLoader))(s"${name} not found")
            loadedModule ← catching(loadedClass.getField("MODULE$").get(null))(s"${name} must be defined as an object")
          } yield loadedModule
        }
        .toList
        .sequenceU
      result ← libraries.map { library ⇒
        Xor.catchNonFatal(
          compiler.compile(
          library,
          (libraryNames ++ sectionNames).toSet.flatMap(analysisIn.relations.definesClass)
          .map(file ⇒ IO.read(file)).toArray,
          "defaultLib"
        ).toList
        ).leftMap(_.getMessage).flatMap {
          _ match {
            case moduleName :: moduleSource :: Nil ⇒ Xor.right(moduleName → moduleSource)
            case _                                 ⇒ Xor.left("Unexpected return value from exercise compiler")
          }
        }
      }
        .sequenceU
    } yield result

    result.fold(
      errorMessage ⇒ throw new Exception(errorMessage), { value ⇒ value }
    )
  }

  def generateExerciseSourcesTask = Def.task {
    val log = streams.value.log
    lazy val preGen = preGenEx.value
    val dir = (sourceManaged in CompileExercises).value

    preGen.map {
      case (name, code) ⇒
        val file = dir / (name.replace(".", "/") + ".scala")
        IO.write(file, code)
        log.info(s"Generated library at $file")
        file
    }
  }

  def generateExerciseDescriptorTask = Def.task {
    val log = streams.value.log
    val preGen = preGenEx.value
    val qualifiedLibraryInstancies = preGen.map(_._1 + "$")
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
