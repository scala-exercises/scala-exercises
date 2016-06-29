/*
 * scala-exercises-definitions
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package exercise

/** Marker trait for exercise libraries.
  */
trait Library {
  def owner: String
  def repository: String
  def sections: List[Section]
  def color: Option[String] = None
}
