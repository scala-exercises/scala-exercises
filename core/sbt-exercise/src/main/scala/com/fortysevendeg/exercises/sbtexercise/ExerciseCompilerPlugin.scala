
package com.fortysevendeg.exercises
package sbtexercise

import scala.language.reflectiveCalls

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

  val generateExercises = TaskKey[List[(String, String)]]("pregen-exercises")

  object autoImport {
    def CompileExercises = ExerciseCompilerPlugin.CompileExercises
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

      generateExercises <<= generateExercisesTask
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

  // reflection is used to invoke a java-style interface to the exercise compiler
  private val COMPILER_CLASS = "com.fortysevendeg.exercises.compiler.CompilerJava"
  private type COMPILER = {
    def compile(library: AnyRef, sources: Array[String], targetPackage: String): Array[String]
  }

  // worker task that invokes the exercise compiler
  def generateExercisesTask = Def.task {
    val log = streams.value.log
    log.info("compiling scala exercises")

    lazy val analysisIn = (compile in CompileExercisesSource).value

    lazy val libraryNames = discoverLibraries(analysisIn)
    lazy val sectionNames = discoverSections(analysisIn)

    val libraryClasspath = Attributed.data((fullClasspath in CompileExercisesSource).value)

    val loader = ClasspathUtilities.toLoader(
      (Meta.compilerClasspath ++ libraryClasspath).distinct,
      null,
      ClasspathUtilities.createClasspathResources(
        appPaths = Meta.compilerClasspath,
        bootPaths = scalaInstance.value.jars
      )
    )

    def loadLibraryModule(name: String) = for {
      loadedClass ← catching(loader.loadClass(name + "$"))(s"${name} not found")
      loadedModule ← catching(loadedClass.getField("MODULE$").get(null))(s"${name} must be defined as an object")
    } yield loadedModule

    def invokeCompiler(compiler: COMPILER, library: AnyRef): Xor[String, (String, String)] =
      Xor.catchNonFatal {
        val sourceCodes = (libraryNames ++ sectionNames).toSet
          .flatMap(analysisIn.relations.definesClass)
          .map(IO.read(_))

        compiler.compile(
          library = library,
          sources = sourceCodes.toArray,
          targetPackage = "defaultLib"
        ).toList
      } leftMap (_.getMessage) >>= {
        _ match {
          case moduleName :: moduleSource :: Nil ⇒ Xor.right(moduleName → moduleSource)
          case _                                 ⇒ Xor.left("Unexpected return value from exercise compiler")
        }
      }

    val result = for {
      compilerClass ← catching(loader.loadClass(COMPILER_CLASS))("Unable to find exercise compiler class")
      compiler ← catching(compilerClass.newInstance.asInstanceOf[COMPILER])("Unable to create instance of exercise compiler")
      libraries ← libraryNames.map(loadLibraryModule).toList.sequenceU
      result ← libraries.map(invokeCompiler(compiler, _)).sequenceU
    } yield result

    result.fold(message ⇒ throw new Exception(message), { value ⇒ value })
  }

  // task responsible for outputting the source files
  def generateExerciseSourcesTask = Def.task {
    val log = streams.value.log
    val generated = generateExercises.value
    val dir = (sourceManaged in CompileExercises).value
    generated.map {
      case (name, code) ⇒
        val file = dir / (name.replace(".", "/") + ".scala")
        IO.write(file, code)
        log.info(s"Generated library at $file")
        file
    }
  }

  // task responsible for outputting the exercise descriptor resource
  def generateExerciseDescriptorTask = Def.task {
    val log = streams.value.log
    val generated = generateExercises.value
    val qualifiedLibraryInstancies = generated.map(_._1 + "$")
    val dir = (resourceManaged in CompileExercises).value
    val resourceFile = dir / "scala-exercises" / "library.47"
    IO.write(resourceFile, qualifiedLibraryInstancies.mkString("\n"))
    Seq(resourceFile)
  }

}
