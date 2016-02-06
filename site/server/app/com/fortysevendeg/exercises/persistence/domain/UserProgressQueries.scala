package com.fortysevendeg.exercises.persistence.domain

object SaveUserProgress {

  case class Request(
    userId:      Long,
    libraryName: String,
    sectionName: String,
    method:      String,
    args:        Option[String],
    succeeded:   Boolean
  )
}

object UserProgressQueries {

  val allFields = List("userId", "libraryName", "sectionName", "method", "args", "succeeded")

  val findByUserId =
    """
          SELECT id,
          userId,
          libraryName,
          sectionName,
          method,
          args,
          succeeded
          FROM UserProgress
          WHERE userId = ?"""

  val update =
    """
          UPDATE UserProgress
          SET libraryName = ?,
          sectionName = ?,
          method = ?,
          args = ?,
          succeeded = ?
          WHERE id = ?
      """

  val insert =
    """
          INSERT INTO UserProgress(userId, libraryName, sectionName, method, args, succeeded)
          VALUES(?, ?, ?, ?, ?, ?)
      """

  val deleteById =
    "DELETE FROM UserProgress WHERE id = ?"
}
