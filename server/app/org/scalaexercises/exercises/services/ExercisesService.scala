/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises
package services

import org.scalaexercises.runtime.{ Exercises, MethodEval, Timestamp }
import org.scalaexercises.runtime.model.{ Library ⇒ RuntimeLibrary, Section ⇒ RuntimeSection, Exercise ⇒ RuntimeExercise, Contribution ⇒ RuntimeContribution, DefaultLibrary }

import org.scalaexercises.types.exercises.{ Library, Section, Exercise, Contribution, ExerciseEvaluation }

import play.api.Play
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

  val methodEval = new MethodEval()

  def classLoader = Play.maybeApplication.fold(ExercisesService.getClass.getClassLoader)(_.classloader)
  lazy val (errors, runtimeLibraries) = Exercises.discoverLibraries(cl = classLoader)

  lazy val (libraries: List[Library], librarySections: Map[String, List[Section]]) = {
    val libraries1 = colorize(runtimeLibraries)
    errors.foreach(error ⇒ Logger.warn(s"$error")) // TODO: handle errors better?
    (
      libraries1.map(convertLibrary),
      libraries1.map(library0 ⇒ library0.name → library0.sections.map(convertSection)).toMap
    )
  }

  def section(libraryName: String, name: String): Option[Section] =
    librarySections.get(libraryName) >>= (_.find(_.name == name))

  def evaluate(evaluation: ExerciseEvaluation): ExerciseEvaluation.Result = {

    def compileError(ef: EvaluationFailure[_]): String =
      s"Compilation error: ${ef.foldedException.getMessage}"

    def userError(ee: EvaluationException[_]): String = ee.e match {
      case _: TestFailedException ⇒ "Assertion error!"
      case e                      ⇒ s"Runtime error: ${e.getClass.getName}"
    }

    def eval(pkg: String, imports: List[String]) = {
      val res = methodEval.eval(
        pkg,
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

    } yield (runtimeExercise.packageName, runtimeExercise.imports)

    val res = imports >>= (eval _).tupled
    Logger.info(s"evaluation for $evaluation: $res")
    res

  }

  def reorderLibraries(topLibNames: List[String], libraries: List[Library]): List[Library] = {
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
  // not particularly clean, but this assigns colors
  // to libraries that don't have a default color provided
  // TODO: make this nicer
  def colorize(libraries: List[RuntimeLibrary]): List[RuntimeLibrary] = {
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

    val (_, res) = libraries.foldLeft((autoPalette, Nil: List[RuntimeLibrary])) { (acc, library) ⇒
      val (colors, librariesAcc) = acc
      if (library.color.isEmpty) {
        val (color, colors0) = colors match {
          case head :: tail ⇒ Some(head) → tail
          case Nil          ⇒ None → Nil
        }
        colors0 → (DefaultLibrary(
          owner = library.owner,
          repository = library.repository,
          name = library.name,
          description = library.description,
          color = color,
          sections = library.sections,
          timestamp = Timestamp.fromDate(new java.util.Date())
        ) :: librariesAcc)
      } else
        colors → (library :: librariesAcc)
    }
    res.reverse
  }

  def convertLibrary(library: RuntimeLibrary): Library =
    Library(
      owner = library.owner,
      repository = library.repository,
      name = library.name,
      description = library.description,
      color = library.color getOrElse "black",
      sections = library.sections map convertSection,
      timestamp = library.timestamp
    )

  def convertSection(section: RuntimeSection): Section =
    Section(
      name = section.name,
      description = section.description,
      path = section.path,
      exercises = section.exercises.map(convertExercise),
      contributions = section.contributions.map(convertContribution)
    )

  def convertExercise(exercise: RuntimeExercise): Exercise =
    Exercise(
      method = exercise.qualifiedMethod,
      name = Option(exercise.name),
      description = exercise.description,
      code = Option(exercise.code),
      explanation = exercise.explanation
    )

  def convertContribution(contribution: RuntimeContribution): Contribution =
    Contribution(
      sha = contribution.sha,
      message = contribution.message,
      timestamp = contribution.timestamp,
      url = contribution.url,
      author = contribution.author,
      authorUrl = contribution.authorUrl,
      avatarUrl = contribution.avatarUrl
    )

}
