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
  exerciseType: String,
  args:         List[String]
)

object ExerciseEvaluation {
  // TODO: create shared layer ADT for this type, or make type in
  // runtime project available in this scope?
  // The right projection needs to indicate a perfect run, as
  // user progress is updated when this Xor isRight!
  // The left projection should capture all failure scenarios.
  type Result = Xor[Throwable, Throwable] Xor Any
}
