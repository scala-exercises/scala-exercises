/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package messages

case class EvaluationRequest(
  libraryName:  String,
  sectionName:  String,
  method:       String,
  version:      Int,
  exerciseType: String,
  args:         Seq[String]
)

// TODO: moar info
case class EvaluationResult(
  ok:     Boolean,
  method: String,
  msg:    String  = ""
)
