package services

import models.{ ExerciseEvaluation, Section, Category }
import scalaz.\/

object ExercisesService {

  def sections: List[Section] =
    exerciseV0.ExercisesService.sections

  def category(section: String, category: String): List[Category] =
    exerciseV0.ExercisesService.category(section, category)

  def evaluate(evaluation: ExerciseEvaluation): Throwable \/ Unit =
    exerciseV0.ExercisesService.evaluate(evaluation)

}
