package services

import models.{ ExerciseEvaluation, Section, Category }
import scalaz.\/

object ExercisesService {

  /** Scans the classpath returning a list of all Sections found
    * A section is defined by a folder with a package object including section information.
    * Section packages should be nested under `exercises`
    * @see exercises.stdlib
    */
  def sections: List[Section] =
    exerciseV0.ExercisesService.sections

  /** Scans the classpath returning a list of categories containing exercises for a given section
    */
  def category(section: String, category: String): List[Category] =
    exerciseV0.ExercisesService.category(section, category)

  /** Evaluates an exercise in a given section and category and returns a disjunction
    * containing either an exception or Unit representing success
    */
  def evaluate(evaluation: ExerciseEvaluation): Throwable \/ Unit =
    exerciseV0.ExercisesService.evaluate(evaluation)

}
