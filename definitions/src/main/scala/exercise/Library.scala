/*
 * scala-exercises-definitions
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.definitions

import cats.data.NonEmptyList

/** Marker trait for exercise libraries.
  */
trait Library {
  def owner: String
  def repository: String
  def sections: NonEmptyList[Section]
  def color: Option[String] = None
}
