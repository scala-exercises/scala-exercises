/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._
import play.api.libs.json._

trait JsonFormats {

  implicit val exerciseWrites: Writes[Exercise] = Json.writes[Exercise]

  implicit val contributionWrites: Writes[Contribution] = Json.writes[Contribution]

  implicit val sectionWrites: Writes[Section] = Json.writes[Section]

  implicit val buildInfoWrites: Writes[BuildInfo] = Json.writes[BuildInfo]

  implicit val libraryWrites: Writes[Library] = Json.writes[Library]

  implicit val exerciseProgressWrites: Writes[ExerciseProgress] = Json.writes[ExerciseProgress]

  implicit val sectionExercisesWrites: Writes[SectionExercises] = Json.writes[SectionExercises]

  implicit val exerciseTypeReads: Reads[ExerciseType] =
    JsPath.read[String].map(ExerciseType.fromString)

  implicit val exerciseEvaluationReads: Reads[ExerciseEvaluation] = Json.reads[ExerciseEvaluation]

}

object JsonFormats extends JsonFormats
