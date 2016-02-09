package com.fortysevendeg.exercises.persistence.domain

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
  )

}

object UserProgressQueries {

  val allFields = List("userId", "libraryName", "sectionName", "method", "version", "exerciseType", "args", "succeeded")

  private[this] val commonFindBy =
    s"""
          SELECT
          ${allFields.mkString(", ")}
          FROM \"UserProgress\"
          WHERE """

  val findByUserId =
    s"""$commonFindBy userId = ?"""

  val findByExerciseVersion =
    s"""$commonFindBy userId = ? AND libraryName = ? AND sectionName = ? AND method = ? AND version = ?"""

  val update =
    """
          UPDATE \"UserProgress\"
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
          INSERT INTO \"UserProgress\"(${allFields.mkString(", ")})
          VALUES(?, ?, ?, ?, ?, ?)
    """

  val deleteById =
    "DELETE FROM \"UserProgress\" WHERE id = ?"
}
