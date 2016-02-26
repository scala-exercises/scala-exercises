/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.domain

import shared._
import shapeless._
import ops.record._
import doobie.imports._

object SaveUserProgress {

  case class Request(
      userId:       Long,
      libraryName:  String,
      sectionName:  String,
      method:       String,
      version:      Int,
      exerciseType: ExerciseType,
      args:         List[String],
      succeeded:    Boolean
  ) {

    def asUserProgress(id: Long): UserProgress =
      UserProgress(id, userId, libraryName, sectionName, method, version, exerciseType, args, succeeded)
  }

}

object UserProgressQueries {

  val userProgressGen = LabelledGeneric[shared.UserProgress]
  val userProgressKeys = Keys[userProgressGen.Repr]
  val allFields: List[String] =
    userProgressKeys()
      .to[List]
      .map { case Symbol(s) ⇒ s.toLowerCase }

  object Implicits {
    implicit val ExerciseTypeMeta: Meta[ExerciseType] =
      Meta[String].nxmap(
        ExerciseType.fromString,
        ExerciseType.toString
      )
  }

  private[this] val commonFindBy =
    """SELECT
       id, userid, libraryname, sectionname, method, version, exercisetype, args, succeeded
       FROM "userProgress""""

  val all = commonFindBy

  val findById = s"$commonFindBy WHERE id = ?"

  val findByUserId = s"$commonFindBy WHERE userId = ?"

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
  s"""$commonFindBy WHERE userId = ? AND libraryName = ?"""

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
