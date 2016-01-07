package models

/** A library representing a lib or lang. Ej. stdlib, cats, scalaz...
  */
case class Library(
  title: String,
  description: String,
  color: String,
  sections: List[String] = Nil)

/** A section in a library. For example `Extractors`
  */
case class Section(
  title: String,
  description: Option[String] = None,
  exercises: List[Exercise] = Nil)

/** Exercises within a Category
  */
case class Exercise(
  method: Option[String] = None,
  title: Option[String] = None,
  description: Option[String] = None,
  code: Option[String] = None,
  explanation: Option[String] = None)

/** Input params necessary to evaluate an exercise
  */
case class ExerciseEvaluation(
  library: String,
  section: String,
  method: String,
  args: List[String])
