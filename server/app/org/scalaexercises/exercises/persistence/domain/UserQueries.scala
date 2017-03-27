/*
 * scala-exercises - server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.persistence.domain

import github4s.free.domain.{User ⇒ GHUser}
import org.scalaexercises.types.user.User

object UserCreation {

  abstract class CreationError extends Product with Serializable

  case object DuplicateName extends CreationError

  case class Request(
      login: String,
      name: Option[String],
      githubId: String,
      pictureUrl: String,
      githubUrl: String,
      email: Option[String]
  ) {

    def asUser(id: Long): User =
      User(id, login, name, githubId, pictureUrl, githubUrl, email)
  }

  def toUser(ghu: GHUser) =
    Request(ghu.login, ghu.name, ghu.id.toString, ghu.avatar_url, ghu.html_url, ghu.email)

  type Response = Either[CreationError, User]
}

object UserQueries {

  val allFields: List[String] = fieldNames[User]()

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
