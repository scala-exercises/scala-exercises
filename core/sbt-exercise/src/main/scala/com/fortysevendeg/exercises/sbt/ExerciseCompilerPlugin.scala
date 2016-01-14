
package com.fortysevendeg.exercises
package sbt

import _root_.sbt._
import _root_.sbt.Keys._

import scala.io.Codec

// This was based off of the twirl sbt plugin

object ExerciseCompilerPlugin extends AutoPlugin {

  override def requires = plugins.JvmPlugin

  override def trigger = noTrigger

  object Keys {
    val compileExercises = TaskKey[Seq[File]]("47-compile-exercises", "Compile scala exercises")
  }

  import Keys._

  println("WAHOO: exercise compiler plugin says hi")

  def scalacEncoding(options: Seq[String]): String = {
    val i = options.indexOf("-encoding") + 1
    if (i > 0 && i < options.length) options(i) else "UTF-8"
  }

  def compileExercisesTask = Def.task {
    ExerciseCompiler.compile(
      (sourceDirectories in compileExercises).value,
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
  def compile(
    sourceDirectories: Seq[File],
    targetDirectory:   File,
    includeFilter:     FileFilter,
    excludeFilter:     FileFilter,
    codec:             Codec,
    log:               Logger
  ) = {
    println("insert forked compilation here")
    Nil
  }
}
