package org.scalaexercises.types.user

import cats.data.Xor

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

  type Response = CreationError Xor User
}
