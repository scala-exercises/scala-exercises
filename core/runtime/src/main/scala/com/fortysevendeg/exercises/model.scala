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
