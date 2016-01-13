package services

import shared._
import scalaz.\/

object ExercisesService {

  def libraries: List[Library] =
    exerciseV0.ExercisesService.libraries

  def section(library: String, name: String): List[Section] =
    exerciseV0.ExercisesService.section(library, name)

  def evaluate(evaluation: ExerciseEvaluation): Throwable \/ Unit =
    exerciseV0.ExercisesService.evaluate(evaluation)

}
