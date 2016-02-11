package com.fortysevendeg.exercises.services

import com.fortysevendeg.exercises.Exercises

import play.api.Logger

import cats.data.Xor
import cats.std.option._
import cats.syntax.flatMap._

object ExercisesService extends RuntimeSharedConversions {

  val (libraries, librarySections) = {
    val (errors, libraries0) = Exercises.discoverLibraries(cl = ExercisesService.getClass.getClassLoader)
    errors.foreach(error ⇒ Logger.warn(s"$error")) // TODO: handle errors better?
    (
      libraries0.map(convertLibrary),
      libraries0.map(library0 ⇒ library0.name → library0.sections.map(convertSection)).toMap
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

  def convertLibrary(library: Library) =
    shared.Library(
      name = library.name,
      description = library.description,
      color = library.color,
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
      method = None, // exercise.eval Option[type Input => Unit]
      name = exercise.name,
      description = exercise.code,
      code = exercise.code,
      explanation = exercise.explanation
    )

}
