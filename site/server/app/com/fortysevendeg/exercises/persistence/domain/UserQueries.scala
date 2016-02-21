/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.domain

import cats.data.Xor
import shared.User
import shapeless._
import ops.record._

object UserCreation {

  abstract class CreationError extends Product with Serializable

  case object DuplicateName extends CreationError

  case class Request(
      login:      String,
      name:       Option[String],
      githubId:   String,
      pictureUrl: String,
      githubUrl:  String,
      email:      Option[String]
  ) {

    def asUser(id: Long): User =
      User(id, login, name, githubId, pictureUrl, githubUrl, email)
  }

  type Response = CreationError Xor User
}

object UserQueries {

  val userGen = LabelledGeneric[shared.User]
  val userKeys = Keys[userGen.Repr]
  val allFields: List[String] =
    userKeys()
      .to[List]
      .map { case Symbol(s) ⇒ s.toLowerCase }

  private[this] val commonFindBy =
    """SELECT
       id, login, name, githubId, pictureUrl, githubUrl, email
       FROM users"""

  val all = commonFindBy

  val findById = s"$commonFindBy WHERE id = ?"

  val findByLogin = s"$commonFindBy WHERE login = ?"

  val update =
    s"""
          UPDATE users
          SET
          name = ?,
          githubId = ?,
          pictureUrl = ?,
          githubUrl = ?,
          email = ?
          WHERE id = ?
    """

  val insert =
    s"""
          INSERT INTO users(${allFields.tail.mkString(", ")})
          VALUES(?,?,?,?,?,?)
    """

  val deleteById = "DELETE FROM users WHERE id = ?"

  val deleteAll = "DELETE FROM users"
}
