/*
 *  scala-exercises
 *
 *  Copyright 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
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
 *
 */

package org.scalaexercises.exercises.services.handlers

import cats.effect.Sync
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.exercises.services.ExercisesService
import org.scalaexercises.types.exercises.ExerciseEvaluation.EvaluationRequest
import org.scalaexercises.types.exercises.{ExerciseEvaluation, Library, Section}
import play.api.Application

class ExerciseOpsHandler[F[_]](app: Application)(implicit F: Sync[F]) extends ExerciseOps[F] {
  override def getLibraries: F[List[Library]] = F.delay(ExercisesService(app).libraries)

  override def getSection(libraryName: String, sectionName: String): F[Option[Section]] =
    F.delay(ExercisesService(app).section(libraryName, sectionName))

  override def buildRuntimeInfo(evaluation: ExerciseEvaluation): F[EvaluationRequest] =
    F.delay(ExercisesService(app).buildRuntimeInfo(evaluation))
}
