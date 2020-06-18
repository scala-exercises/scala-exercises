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
import doobie.util.transactor.Transactor
import doobie.implicits._
import org.scalaexercises.algebra.progress.UserProgressOps
import org.scalaexercises.exercises.persistence.repositories.UserProgressRepository
import org.scalaexercises.types.progress.{SaveUserProgress, UserProgress}
import org.scalaexercises.types.user.User

class UserProgressOpsHandler[F[_]](implicit F: Sync[F], T: Transactor[F])
    extends UserProgressOps[F] {

  private val repo = UserProgressRepository.instance

  override def saveUserProgress(userProgress: SaveUserProgress.Request): F[UserProgress] =
    repo.upsert(userProgress).transact(T)

  override def getExerciseEvaluations(
      user: User,
      library: String,
      section: String
  ): F[List[UserProgress]] =
    repo.getExerciseEvaluations(user, library, section).transact(T)

  override def getLastSeenSection(user: User, library: String): F[Option[String]] =
    repo.getLastSeenSection(user, library).transact(T)
}
