/*
 * scala-exercises - core
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.types.evaluator

final case class Dependency(
  groupId:    String,
  artifactId: String,
  version:    String
)
