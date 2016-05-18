/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises
package services

import com.fortysevendeg.exercises.Exercises
import com.fortysevendeg.exercises.MethodEval

import play.api.Logger

import java.nio.file.Paths

import cats.data.Xor
import cats.data.Ior
import cats.std.option._
import cats.syntax.flatMap._
import cats.syntax.option._

import org.scalatest.exceptions.TestFailedException

object ExercisesService extends RuntimeSharedConversions {
  import MethodEval._

  // using BuildInfo as a shortcut this will point to the wrong path when deployed
  val artifacts = buildinfo.BuildInfo.dependencyClasspath.distinct.map(v ⇒ Paths.get(v.toURI))
  val methodEval = new MethodEval(
    artifacts, 
    // play does not fork the jvm
    // this should also be revisited for deployment
    security = false
  )

  val (errors, runtimeLibraries) = Exercises.discoverLibraries(cl = ExercisesService.getClass.getClassLoader)
  val (libraries, librarySections) = {
    val libraries1 = colorize(runtimeLibraries)
    errors.foreach(error ⇒ Logger.warn(s"$error")) // TODO: handle errors better?
    (
      libraries1.map(convertLibrary),
      libraries1.map(library0 ⇒ library0.name → library0.sections.map(convertSection)).toMap
    )
  }

  def section(libraryName: String, name: String): Option[shared.Section] =
    librarySections.get(libraryName) >>= (_.find(_.name == name))

  def evaluate(evaluation: shared.ExerciseEvaluation): shared.ExerciseEvaluation.Result = {

    def compileError(ef: EvaluationFailure[_]): String =
      s"Compilation error: ${ef.foldedException.getMessage}"

    def userError(ee: EvaluationException[_]): String = ee.e match {
      case _: TestFailedException ⇒ "Assertion error!"
      case e                      ⇒ s"Runtime error: ${e.getMessage}"
    }

    def eval(imports: List[String]) = {
      val res = methodEval.eval(
        evaluation.method,
        evaluation.args,
        imports
      )
      res.toSuccessXor.bimap(
        _.fold(compileError(_), userError(_)),
        _ ⇒ Unit
      )
    }

    val imports = for {

      runtimeLibrary ← runtimeLibraries.find(_.name == evaluation.libraryName)
        .toRightXor(s"Unable to find library ${evaluation.libraryName} when " +
          s"attempting to evaluate method ${evaluation.method}")

      runtimeSection ← runtimeLibrary.sections.find(_.name == evaluation.sectionName)
        .toRightXor(s"Unable to find section ${evaluation.sectionName} when " +
          s"attempting to evaluate method ${evaluation.method}")

      runtimeExercise ← runtimeSection.exercises.find(_.qualifiedMethod == evaluation.method)
        .toRightXor(s"Unable to find exercise for method ${evaluation.method}")

    } yield runtimeExercise.imports

    val res = imports >>= eval
    Logger.info(s"evaluation for $evaluation: $res")
    res

  }

  def reorderLibraries(topLibNames: List[String], libraries: List[shared.Library]): List[shared.Library] = {
    libraries.sortBy(lib ⇒ {
      val idx = topLibNames.indexOf(lib.name)
      if (idx == -1)
        Integer.MAX_VALUE
      else
        idx
    })
  }
}

sealed trait RuntimeSharedConversions {
  import com.fortysevendeg.exercises._

  // not particularly clean, but this assigns colors
  // to libraries that don't have a default color provided
  // TODO: make this nicer
  def colorize(libraries: List[Library]): List[Library] = {
    libraries
    val autoPalette = List(
      "#00587A",
      "#44BBFF",
      "#EBF680",
      "#66CC99",
      "#FCA65F",
      "#112233",
      "#FC575E",
      "#CDCBA6",
      "#37465D",
      "#DD6F47",
      "#6AB0AA",
      "#008891",
      "#0F3057"
    )

    val (_, res) = libraries.foldLeft((autoPalette, Nil: List[Library])) { (acc, library) ⇒
      val (colors, librariesAcc) = acc
      if (library.color.isEmpty) {
        val (color, colors0) = colors match {
          case head :: tail ⇒ Some(head) → tail
          case Nil          ⇒ None → Nil
        }
        colors0 → (DefaultLibrary(
          name = library.name,
          description = library.description,
          color = color,
          sections = library.sections
        ) :: librariesAcc)
      } else
        colors → (library :: librariesAcc)
    }
    res.reverse
  }

  def convertLibrary(library: Library) =
    shared.Library(
      name = library.name,
      description = library.description,
      color = library.color getOrElse "black",
      sections = library.sections map convertSection
    )

  def convertSection(section: Section) =
    shared.Section(
      name = section.name,
      description = section.description,
      exercises = section.exercises.map(convertExercise)
    )

  def convertExercise(exercise: Exercise) =
    shared.Exercise(
      method = exercise.qualifiedMethod,
      name = Option(exercise.name),
      description = exercise.description,
      code = Option(exercise.code),
      explanation = exercise.explanation
    )

}
