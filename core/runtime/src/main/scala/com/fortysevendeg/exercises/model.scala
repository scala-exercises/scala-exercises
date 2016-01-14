package com.fortysevendeg.exercises

// This is the exercise runtime metamodel

/** An exercise library.
  */
trait Library {
  def name: String
  def description: String
  def color: String
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
  def name: Option[String]
}

// default case class implementations

case class DefaultLibrary(
  name:        String,
  description: String,
  color:       String,
  sections:    List[Section] = Nil
) extends Library

case class DefaultSection(
  name:        String,
  description: Option[String] = None,
  exercises:   List[Exercise] = Nil
) extends Section

case class DefaultExercise(
  name: Option[String] = None
) extends Exercise
