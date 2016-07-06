/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._
import play.api.libs.json._

trait JsonFormats {

  implicit val exerciseWrites: Writes[Exercise] = Json.writes[Exercise]

  implicit val contributionWrites: Writes[Contribution] = Json.writes[Contribution]

  implicit val sectionWrites: Writes[Section] = Json.writes[Section]

  implicit val libraryWrites: Writes[Library] = Json.writes[Library]

  implicit val exerciseProgressWrites: Writes[ExerciseProgress] = Json.writes[ExerciseProgress]

  implicit val sectionExercisesWrites: Writes[SectionExercises] = Json.writes[SectionExercises]

  implicit val exerciseTypeReads: Reads[ExerciseType] = JsPath.read[String].map(ExerciseType.fromString)

  implicit val exerciseEvaluationReads: Reads[ExerciseEvaluation] = Json.reads[ExerciseEvaluation]

}

object JsonFormats extends JsonFormats
