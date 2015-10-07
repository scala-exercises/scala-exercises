package models

/**
 * A section representing a lib or lang. Ej. stdlib, cats, scalaz...
 */
case class Section(
    title : String,
    description : String,
    categories : List[String] = Nil)

/**
 * A category. For example `Extractors`
 */
case class Category(
    title : String,
    description : Option[String] = None,
    exercises : List[Exercise] = Nil)

/**
 * Exercises within a Category
 */
case class Exercise(
    method : Option[String] = None,
    title : Option[String] = None,
    description : Option[String] = None,
    code : Option[String] = None)

/**
 * Input params necessary to evaluate an exercise
 */
case class ExerciseEvaluation(
    section : String,
    category : String,
    method : String,
    args : List[String])