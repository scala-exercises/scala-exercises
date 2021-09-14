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

package org.scalaexercises.algebra.progress

import cats.Functor
import cats.implicits._
import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._
import org.scalaexercises.types.user._

/**
 * Exposes User Progress operations as a Free monadic algebra that may be combined with other
 * Algebras via Coproduct
 */
trait UserProgressOps[F[_]] {
  def saveUserProgress(userProgress: SaveUserProgress.Request): F[UserProgress]

  def getExerciseEvaluations(user: User, library: String, section: String): F[List[UserProgress]]

  def getLastSeenSection(user: User, library: String): F[Option[String]]

  def getSolvedExerciseCount(user: User, library: String, section: Section)(implicit
      F: Functor[F]
  ): F[Int] =
    getExerciseEvaluations(user, library, section.name).map(tried =>
      tried.count(UP => UP.succeeded && section.exercises.exists(_.method == UP.method))
    )

  def isSectionCompleted(user: User, libraryName: String, section: Section)(implicit
      F: Functor[F]
  ): F[Boolean] =
    getSolvedExerciseCount(user, libraryName, section).map(solvedExercises =>
      solvedExercises == section.exercises.size
    )

}
