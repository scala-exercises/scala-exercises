package services

import play.api.Logger

import com.fortysevendeg.exercises.Exercises

import scalaz.\/

object ExercisesService extends RuntimeSharedConversions {

  object internal {
    // TODO: handle errors?
    lazy val (errors, libraries) = Exercises.discoverLibraries(cl = ExercisesService.getClass.getClassLoader)
  }

  def libraries: List[shared.Library] =
    internal.libraries.map(convertLibrary)

  def section(libraryName: String, name: String): List[shared.Section] = {
    internal.libraries.find(_.name == libraryName) match {
      case Some(library) ⇒ library.sections.map(convertSection)
      case None ⇒
        Logger.warn(s"Unable to find exercise $name in library $libraryName")
        Nil
    }
  }

  def evaluate(evaluation: shared.ExerciseEvaluation): Throwable \/ Unit = ???

}

// This should all go away if we reconsile the two separate models (runtime vs shared)

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
