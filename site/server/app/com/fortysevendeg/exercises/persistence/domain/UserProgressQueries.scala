package com.fortysevendeg.exercises.persistence.domain

import shared.UserProgress

object SaveUserProgress {

  sealed trait ExerciseType

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
    s"""
          SELECT
          ${allFields.mkString(", ")}
          FROM "userProgress"
          WHERE """

  val findById = s"""$commonFindBy id = ?"""

  val findByUserId = s"""$commonFindBy userId = ?"""

  val findByExerciseVersion =
    s"""$commonFindBy userId = ? AND libraryName = ? AND sectionName = ? AND method = ? AND version = ?"""

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
