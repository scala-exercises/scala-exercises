
package com.fortysevendeg.exercises
package sbt

import _root_.sbt._
import _root_.sbt.Keys._

import scala.io.Codec
import scala.util.{ Try, Success, Failure }

import java.io.File
import java.net.URLClassLoader

object ExerciseCompilerPlugin extends AutoPlugin {

  override def requires = plugins.JvmPlugin

  override def trigger = noTrigger

  object ExerciseCompilerKeys {
    val compileExercises = TaskKey[Seq[File]]("compileExercises", "Compile scala exercises")
  }

  import ExerciseCompilerKeys._

  def scalacEncoding(options: Seq[String]): String = {
    val i = options.indexOf("-encoding") + 1
    if (i > 0 && i < options.length) options(i) else "UTF-8"
  }

  def compileExercisesTask = Def.task {
    ExerciseCompiler.compile(
      (sources in compileExercises).value,
      (target in compileExercises).value,
      (includeFilter in compileExercises).value,
      (excludeFilter in compileExercises).value,
      Codec(scalacEncoding(scalacOptions.value)),
      streams.value.log
    )
  }

  def exerciseSettings: Seq[Setting[_]] = Seq(
    includeFilter in compileExercises := "*.scala",
    excludeFilter in compileExercises := HiddenFileFilter,
    sourceDirectories in compileExercises := Seq(sourceDirectory.value / "exercises"),

    sources in compileExercises <<= Defaults.collectFiles(
      sourceDirectories in compileExercises,
      includeFilter in compileExercises,
      excludeFilter in compileExercises
    ),

    watchSources in Defaults.ConfigGlobal <++= sources in compileExercises,
    target in compileExercises := crossTarget.value / "exercises" / Defaults.nameForSrc(configuration.value.name),
    compileExercises := compileExercisesTask.value,
    sourceGenerators <+= compileExercises,
    managedSourceDirectories <+= target in compileExercises
  )

  def dependencySettings: Seq[Setting[_]] = Nil

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(exerciseSettings) ++
      inConfig(Test)(exerciseSettings) ++
      dependencySettings

}

object ExerciseCompiler {

  private val COMPILER_MAIN = "com.fortysevendeg.exercises.compiler.CompilerMain"

  def compile(
    sourceDirectories: Seq[File],
    targetDirectory:   File,
    includeFilter:     FileFilter,
    excludeFilter:     FileFilter,
    codec:             Codec,
    log:               Logger
  ) = {

    val exercises = collectExercises(
      sourceDirectories, includeFilter, excludeFilter
    )

    val exercisePaths = exercises.map(_._1.getPath)

    log.info("~" * 20)
    log.info("Launching exercise compiler via new classloader:")
    Meta.compilerClasspath.foreach { entry ⇒
      log.info(s"~ $entry")
    }
    log.info("~" * 20)

    val loader = new URLClassLoader(Meta.compilerClasspath.toArray, null)
    val result = for {
      compilerMainClass ← Try(loader.loadClass(COMPILER_MAIN))
      compilerMain ← Try(compilerMainClass.getMethod("main", classOf[Array[String]]))
    } yield compilerMain.invoke(null, exercisePaths.toArray[String].asInstanceOf[Array[String]])

    result match {
      case Success(v) ⇒ log.info(s"~ result $v")
      case Failure(e) ⇒ log.error(s"~ error ${e.getMessage}")
    }

    log.info("~" * 20)
    log.info("Launching exercise compiler via forked java:")
    log.info("~" * 20)

    val options = ForkOptions(bootJars = Meta.compilerClasspath.map { url ⇒ new File(url.toURI) })
    val exitCode: Int = Fork.java(options, COMPILER_MAIN +: exercisePaths)

    log.info(s"~ forked exit code $exitCode")

    Nil
  }

  def collectExercises(sourceDirectories: Seq[File], includeFilter: FileFilter, excludeFilter: FileFilter): Seq[(File, File)] = {
    sourceDirectories flatMap { sourceDirectory ⇒
      (sourceDirectory ** includeFilter).get flatMap { file ⇒
        if (!excludeFilter.accept(file))
          Some(file → sourceDirectory)
        else
          None
      }
    }
  }

}
