/*
 * scala-exercises - core
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.types.user

case class User(
  id:         Long,
  login:      String,
  name:       Option[String],
  githubId:   String,
  pictureUrl: String,
  githubUrl:  String,
  email:      Option[String]
)

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

  type Response = Either[CreationError, User]
}
