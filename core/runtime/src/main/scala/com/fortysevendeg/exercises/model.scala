package com.fortysevendeg.exercises

// This is the exercise runtime metamodel

/** An exercise library.
  */
trait Library {
  def name: String
  def description: String
  def color: Option[String]
  def sections: List[Section]
}

/** A section in a library.
  */
trait Section {
  def name: String
  def description: Option[String]
  def exercises: List[Exercise]
}

/** Exercises within a section.
  */
trait Exercise {
  type Input

  def name: Option[String]
  def description: Option[String]
  def code: Option[String]
  def eval: Option[Input ⇒ Unit]
  def explanation: Option[String]
}

object Exercise {
  type Aux[A] = Exercise { type Input = A }
}

// default case class implementations
case class DefaultLibrary(
  name:        String,
  description: String,
  color:       Option[String],
  sections:    List[Section]  = Nil
) extends Library

case class DefaultSection(
  name:        String,
  description: Option[String] = None,
  exercises:   List[Exercise] = Nil
) extends Section

case class DefaultExercise[A](
    name:        Option[String]   = None,
    description: Option[String]   = None,
    code:        Option[String]   = None,
    eval:        Option[A ⇒ Unit] = None,
    explanation: Option[String]   = None
) extends Exercise {
  type Input = A
}
