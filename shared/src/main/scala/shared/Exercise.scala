package shared

import cats.data.Ior
import cats.data.Xor

/** A library representing a lib or lang. Ej. stdlib, cats, scalaz...
  */
case class Library(
    name:        String,
    description: String,
    color:       String,
    sections:    List[Section] = Nil
) {
  val sectionNames: List[String] = sections map (_.name)
}

/** A section in a library. For example `Extractors`
  */
case class Section(
  name:        String,
  description: Option[String] = None,
  exercises:   List[Exercise] = Nil
)

/** Exercises within a Category
  */
case class Exercise(
  method:      String,
  name:        Option[String] = None,
  description: Option[String] = None,
  code:        Option[String] = None,
  explanation: Option[String] = None
)

/** Input params necessary to evaluate an exercise
  */
case class ExerciseEvaluation(
  libraryName:  String,
  sectionName:  String,
  method:       String,
  version:      Int,
  exerciseType: ExerciseType,
  args:         List[String]
)

object ExerciseEvaluation {
  type Result = String Xor Any
}

sealed abstract class ExerciseType extends Product with Serializable
case object Koans extends ExerciseType
case object Other extends ExerciseType

object ExerciseType {
  def fromString(s: String): ExerciseType =
    Vector(Koans, Other).find(_.toString == s) getOrElse Other

  def toString(e: ExerciseType): String = e.toString
}

