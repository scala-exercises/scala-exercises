package org.scalaexercises.types.exercises

import cats.data.Xor

/** A library representing a lib or lang. Ej. stdlib, cats, scalaz...
  */
case class Library(
    owner:       String,
    repository:  String,
    name:        String,
    description: String,
    color:       String,
    sections:    List[Section] = Nil,
    timestamp:   String
) {
  val sectionNames: List[String] = sections map (_.name)
  val shortTimestamp: String = timestamp.split("T").headOption.getOrElse(timestamp)
}

/** A section in a library. For example `Extractors`
  */
case class Section(
  name:          String,
  description:   Option[String]     = None,
  path:          Option[String]     = None,
  exercises:     List[Exercise]     = Nil,
  contributions: List[Contribution] = Nil
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

case class Contribution(
  sha:       String,
  message:   String,
  timestamp: String,
  url:       String,
  author:    String,
  authorUrl: String,
  avatarUrl: String
)

case class Contributor(
  author:    String,
  authorUrl: String,
  avatarUrl: String
)
