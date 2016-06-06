/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

// This is the exercise runtime metamodel

/**
 * An exercise library.
 */
trait Library {
  def name: String
  def description: String
  def color: Option[String]
  def sections: List[Section]
}

/**
 * A section in a library.
 */
trait Section {
  def name: String
  def description: Option[String]
  def exercises: List[Exercise]
  def imports: List[String]
}

/**
 * Exercises within a section.
 */
trait Exercise {
  def name: String
  def description: Option[String]
  def code: String
  def qualifiedMethod: String
  def packageName: String
  def imports: List[String]
  def explanation: Option[String]
}

// default case class implementations
case class DefaultLibrary(
  name: String,
  description: String,
  color: Option[String],
  sections: List[Section] = Nil
) extends Library

case class DefaultSection(
  name: String,
  description: Option[String],
  exercises: List[Exercise] = Nil,
  imports: List[String] = Nil
) extends Section

case class DefaultExercise(
  name: String,
  description: Option[String] = None,
  code: String,
  qualifiedMethod: String,
  imports: List[String],
  explanation: Option[String] = None,
  packageName: String
) extends Exercise
