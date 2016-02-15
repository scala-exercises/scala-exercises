/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.controllers

import shared._
import play.api.libs.json.{ Reads, Json, Writes }

trait JsonFormats {

  implicit val exerciseWrites: Writes[Exercise] = Json.writes[Exercise]

  implicit val sectionWrites: Writes[Section] = Json.writes[Section]

  implicit val libraryWrites: Writes[Library] = Json.writes[Library]

  implicit val exerciseEvaluationReads: Reads[ExerciseEvaluation] = Json.reads[ExerciseEvaluation]

}

object JsonFormats extends JsonFormats
