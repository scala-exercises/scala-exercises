/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.domain

import shared.UserProgress
import doobie.imports._

object SaveUserProgress {

  sealed abstract class ExerciseType extends Product with Serializable

  case object Koans extends ExerciseType

  case object Other extends ExerciseType

  case class Request(
      userId:       Long,
      libraryName:  String,
      sectionName:  String,
      method:       String,
      version:      Int,
      exerciseType: ExerciseType   = Other,
      args:         Option[String],
      succeeded:    Boolean
  ) {

    def asUserProgress(id: Long): UserProgress =
      UserProgress(id, userId, libraryName, sectionName, method, version, exerciseType.toString, args, succeeded)
  }

}

object UserProgressQueries {

  val allFields = List("id", "userid", "libraryname", "sectionname", "method", "version", "exercisetype", "args", "succeeded")

  private[this] val commonFindBy =
    """SELECT
       id, userid, libraryname, sectionname, method, version, exercisetype, args, succeeded
       FROM "userProgress""""

  val all = commonFindBy

  val findById = s"$commonFindBy WHERE id = ?"

  val findByUserId = s"""$commonFindBy WHERE userId = ?"""

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
          WHERE id = ?
    """

  val insert =
    s"""
          INSERT INTO "userProgress"(${allFields.tail.mkString(", ")})
          VALUES(?,?,?,?,?,?,?,?)
    """

  val deleteById = s"""DELETE FROM "userProgress" WHERE id = ?"""

  val deleteAll = s"""DELETE FROM "userProgress""""
}
