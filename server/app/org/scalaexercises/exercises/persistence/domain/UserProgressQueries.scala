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

package org.scalaexercises.exercises.persistence.domain

import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._
import doobie._
import doobie.postgres.implicits._

object UserProgressQueries {

  val allFields: List[String] = fieldNames[UserProgress]()

  object Implicits {
    implicit val ExerciseTypeMeta: Meta[ExerciseType] =
      Meta[String].imap(ExerciseType.fromString)(ExerciseType.toString)

    implicit val ListMeta: Meta[List[String]] = Meta[Array[String]].timap(_.toList)(_.toArray)
  }

  private[this] val commonFindBy =
    """SELECT
       id, userid, libraryname, sectionname, method, version, exercisetype, args, succeeded
       FROM "userProgress""""

  val all = commonFindBy

  val findById = s"$commonFindBy WHERE id = ?"

  val findByUserId = s"$commonFindBy WHERE userId = ?"

  val findEvaluationsBySection = s"""$findByUserId AND libraryname = ? AND sectionname = ?"""

  val findEvaluationByVersion = s"""$findEvaluationsBySection AND method = ? AND version = ?"""

  val findByUserIdAggregated =
    s"""
       SELECT libraryname, count(sectionname), bool_and(succeeded)
       FROM "userProgress"
       WHERE userId=?
       GROUP BY libraryname
     """

  val findByLibrary =
    s"""SELECT
       sectionname, bool_and(succeeded)
       FROM "userProgress"
       WHERE userId = ? AND libraryname=?
       GROUP BY sectionname"""

  val findBySection =
    s"""$commonFindBy WHERE userId = ? AND libraryName = ? AND sectionName = ?"""

  val findByExerciseVersion =
    s"""$commonFindBy WHERE userId = ? AND libraryName = ? AND sectionName = ? AND method = ? AND version = ?"""

  val update =
    s"""
          UPDATE "userProgress"
          SET libraryName = ?,
          sectionName = ?,
          method = ?,
          version = ?,
          exerciseType = ?,
          args = ?,
          succeeded = ?
          WHERE userId = ? AND libraryName = ? AND sectionName = ? AND method = ?
    """

  val insert =
    s"""
          INSERT INTO "userProgress"(${allFields.tail.mkString(", ")})
          VALUES(?,?,?,?,?,?,?,?)
    """

  val deleteById = s"""DELETE FROM "userProgress" WHERE id = ?"""

  val deleteAll = s"""DELETE FROM "userProgress""""

  val findLastSeenSection = s"""
      |SELECT sectionName
      |FROM "userProgress"
      |WHERE userId = ?
      |  AND libraryName LIKE ?
      |ORDER BY "updatedAt" DESC
      |LIMIT 1;
  """.stripMargin
}
