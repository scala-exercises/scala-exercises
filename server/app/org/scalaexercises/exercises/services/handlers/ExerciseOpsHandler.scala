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

package org.scalaexercises.exercises.services.handlers

import cats.effect.Sync
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.exercises.services.ExercisesService
import org.scalaexercises.types.exercises.ExerciseEvaluation.EvaluationRequest
import org.scalaexercises.types.exercises.{ExerciseEvaluation, Library, Section}

class ExerciseOpsHandler[F[_]: Sync](implicit F: Sync[F], service: ExercisesService)
    extends ExerciseOps[F] {

  override def getLibraries: F[List[Library]] = F.delay(service.libraries)

  override def getSection(libraryName: String, sectionName: String): F[Option[Section]] =
    F.delay(service.section(libraryName, sectionName))

  override def buildRuntimeInfo(evaluation: ExerciseEvaluation): F[EvaluationRequest] =
    F.delay(service.buildRuntimeInfo(evaluation))
}
