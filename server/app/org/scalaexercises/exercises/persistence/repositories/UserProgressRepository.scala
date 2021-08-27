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

package org.scalaexercises.exercises.persistence.repositories

import org.scalaexercises.types.user._
import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._

import org.scalaexercises.exercises.persistence.PersistenceModule
import org.scalaexercises.exercises.persistence.repositories.UserProgressRepository._
import doobie._
import org.scalaexercises.exercises.persistence.domain.{UserProgressQueries => Q}
import Q.Implicits._

trait UserProgressRepository {
  def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress]

  def findById(id: Long): ConnectionIO[Option[UserProgress]]

  def delete(id: Long): ConnectionIO[Int]

  def update(request: SaveUserProgress.Request): ConnectionIO[UserProgress]

  def upsert(request: SaveUserProgress.Request): ConnectionIO[UserProgress]

  def getExerciseEvaluation(
      user: User,
      libraryName: String,
      sectionName: String,
      method: String,
      version: Int
  ): ConnectionIO[Option[UserProgress]]

  def getExerciseEvaluations(
      user: User,
      libraryName: String,
      sectionName: String
  ): ConnectionIO[List[UserProgress]]

  def getLastSeenSection(user: User, libraryName: String): ConnectionIO[Option[String]]

  def deleteAll(): ConnectionIO[Int]
}

class UserProgressDoobieRepository(implicit persistence: PersistenceModule)
    extends UserProgressRepository {
  override def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress] = {
    val SaveUserProgress.Request(
      user,
      libraryName,
      sectionName,
      method,
      version,
      exerciseType,
      args,
      succeeded
    ) = request
    persistence
      .updateWithGeneratedKeys[InsertParams, UserProgress](
        Q.insert,
        Q.allFields,
        (user.id, libraryName, sectionName, method, version, exerciseType, args, succeeded)
      )
  }

  override def findById(id: Long): ConnectionIO[Option[UserProgress]] =
    persistence.fetchOption[(Long), UserProgress](Q.findById, id)

  override def delete(id: Long): ConnectionIO[Int] =
    persistence.update[(Long)](Q.deleteById, id)

  override def update(request: SaveUserProgress.Request): ConnectionIO[UserProgress] = {
    val SaveUserProgress.Request(
      user,
      libraryName,
      sectionName,
      method,
      version,
      exerciseType,
      args,
      succeeded
    ) = request
    persistence
      .updateWithGeneratedKeys[UpdateParams, UserProgress](
        Q.update,
        Q.allFields,
        (
          libraryName,
          sectionName,
          method,
          version,
          exerciseType,
          args,
          succeeded,
          user.id,
          libraryName,
          sectionName,
          method
        )
      )
  }

  override def upsert(request: SaveUserProgress.Request): ConnectionIO[UserProgress] = {
    val SaveUserProgress.Request(user, libraryName, sectionName, method, version, _, _, _) =
      request
    getExerciseEvaluation(user, libraryName, sectionName, method, version) flatMap {
      case None        => create(request)
      case Some(userP) => update(request)
    }
  }

  override def getExerciseEvaluation(
      user: User,
      libraryName: String,
      sectionName: String,
      method: String,
      version: Int
  ): ConnectionIO[Option[UserProgress]] =
    persistence.fetchOption[FindEvaluationByVersionParams, UserProgress](
      Q.findEvaluationByVersion,
      (user.id, libraryName, sectionName, method, version)
    )

  override def getExerciseEvaluations(
      user: User,
      libraryName: String,
      sectionName: String
  ): ConnectionIO[List[UserProgress]] =
    persistence.fetchList[FindEvaluationsBySectionParams, UserProgress](
      Q.findEvaluationsBySection,
      (user.id, libraryName, sectionName)
    )

  override def getLastSeenSection(user: User, libraryName: String): ConnectionIO[Option[String]] = {
    persistence.fetchOption[FindLastSeenSectionParams, String](
      Q.findLastSeenSection,
      (user.id, libraryName)
    )
  }

  override def deleteAll(): ConnectionIO[Int] =
    persistence.update(Q.deleteAll)
}

object UserProgressRepository {
  type UpdateParams =
    (String, String, String, Int, ExerciseType, List[String], Boolean, Long, String, String, String)
  type InsertParams = (Long, String, String, String, Int, ExerciseType, List[String], Boolean)
  type FindEvaluationByVersionParams  = (Long, String, String, String, Int)
  type FindEvaluationsBySectionParams = (Long, String, String)
  type FindLastSeenSectionParams      = (Long, String)
  type CompletedCountParams           = (Long, String, String)

  implicit def instance(implicit persistence: PersistenceModule): UserProgressRepository =
    new UserProgressDoobieRepository
}
