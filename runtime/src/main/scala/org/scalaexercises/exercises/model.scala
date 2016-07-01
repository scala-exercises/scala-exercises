/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises

// This is the exercise runtime metamodel

/** An exercise library.
  */
trait Library {
  def owner: String
  def repository: String
  def name: String
  def description: String
  def color: Option[String]
  def sections: List[Section]
  def timestamp: String
}

/** A section in a library.
  */
trait Section {
  def name: String
  def description: Option[String]
  def exercises: List[Exercise]
  def imports: List[String]
  def path: Option[String]
  def contributions: List[Contribution]
}

/** A contribution to a section.
  */
trait Contribution {
  def sha: String
  def message: String
  def timestamp: String
  def url: String
  def author: String
  def authorUrl: String
  def avatarUrl: String
}

/** Exercises within a section.
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
  owner:       String,
  repository:  String,
  name:        String,
  description: String,
  color:       Option[String],
  sections:    List[Section]  = Nil,
  timestamp:   String
) extends Library

case class DefaultContribution(
  sha:       String,
  message:   String,
  timestamp: String,
  url:       String,
  author:    String,
  authorUrl: String,
  avatarUrl: String
) extends Contribution

case class DefaultSection(
  name:          String,
  description:   Option[String],
  exercises:     List[Exercise]            = Nil,
  imports:       List[String]              = Nil,
  path:          Option[String]            = None,
  contributions: List[DefaultContribution] = Nil
) extends Section

case class DefaultExercise(
  name:            String,
  description:     Option[String] = None,
  code:            String,
  qualifiedMethod: String,
  imports:         List[String],
  explanation:     Option[String] = None,
  packageName:     String
) extends Exercise
