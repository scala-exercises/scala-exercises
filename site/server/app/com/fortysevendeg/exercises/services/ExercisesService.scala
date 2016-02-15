/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.services

import com.fortysevendeg.exercises.Exercises

import play.api.Logger

import cats.data.Xor
import cats.std.option._
import cats.syntax.flatMap._

object ExercisesService extends RuntimeSharedConversions {

  val (libraries, librarySections) = {
    val (errors, libraries0) = Exercises.discoverLibraries(cl = ExercisesService.getClass.getClassLoader)
    val libraries1 = colorize(libraries0)
    errors.foreach(error ⇒ Logger.warn(s"$error")) // TODO: handle errors better?
    (
      libraries1.map(convertLibrary),
      libraries1.map(library0 ⇒ library0.name → library0.sections.map(convertSection)).toMap
    )
  }

  def section(libraryName: String, name: String): Option[shared.Section] =
    librarySections.get(libraryName) >>= (_.find(_.name == name))

  def evaluate(evaluation: shared.ExerciseEvaluation): Throwable Xor Unit = {
    /* // the previous implementation, for reference
    val evalResult = for {
      pkg ← subclassesOf[exercise.Library]
      library ← ExerciseCodeExtractor.buildLibrary(packageObjectSource(pkg)).toList
      if library.name == evaluation.libraryName
      subclass ← subclassesOf[exercise.Section]
      if simpleClassName(subclass) == evaluation.sectionName
    } yield evaluate(evaluation, subclass)
    evalResult.headOption match {
      case None         ⇒ new RuntimeException("Evaluation produced no results").left[Unit]
      case Some(result) ⇒ result
    }
    */
    Xor.catchNonFatal(???)
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
      sectionNames = library.sections.map(_.name)
    )

  def convertSection(section: Section) =
    shared.Section(
      name = section.name,
      description = section.description,
      exercises = section.exercises.map(convertExercise)
    )

  def convertExercise(exercise: Exercise) =
    shared.Exercise(
      method = exercise.qualifiedMethod, // exercise.eval Option[type Input => Unit]
      name = exercise.name,
      description = exercise.description,
      code = exercise.code,
      explanation = exercise.explanation
    )

}
