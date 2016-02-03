package com.fortysevendeg.exercises.models

import shared.User
import cats.data.Xor
import com.fortysevendeg.exercises.models.queries.Queries
import scalaz.syntax.applicative._

import doobie.imports._
import scalaz.concurrent.Task

object UserCreation {
  // TODO: add proper case objects with errors when switching to Postgres
  sealed trait Error
  case object DuplicateName extends Error

  case class Request(
      login:      String,
      name:       String,
      githubId:   String,
      pictureUrl: String,
      githubUrl:  String,
      email:      String
  ) {

    def asUser(id: Long): User =
      User(id, login, name, githubId, pictureUrl, githubUrl, email)
  }

  type Response = Error Xor User
}

trait UserStore {
  def all: ConnectionIO[List[User]]

  def getByLogin(login: String): ConnectionIO[Option[User]]

  def getById(id: Long): ConnectionIO[Option[User]]

  def create(user: UserCreation.Request): ConnectionIO[UserCreation.Response]

  def delete(id: Long): ConnectionIO[Boolean]

  def update(user: User): ConnectionIO[Option[User]]
}

object UserDoobieStore extends UserStore {
  def all: ConnectionIO[List[User]] =
    Queries.all.list

  def getByLogin(login: String): ConnectionIO[Option[User]] =
    Queries.byLogin(login).option

  def getById(id: Long): ConnectionIO[Option[User]] =
    Queries.byId(id).option

  def create(user: UserCreation.Request): ConnectionIO[UserCreation.Response] = for {
    _ ← Queries.insert(user).run
    user ← getByLogin(user.login)
  } yield if (user.isDefined) Xor.Right(user.get) else Xor.Left(UserCreation.DuplicateName)

  def getOrCreate(user: UserCreation.Request): ConnectionIO[UserCreation.Response] = for {
    maybeUser ← getByLogin(user.login)
    theUser ← if (maybeUser.isDefined)
      Xor.Right(maybeUser.get).point[ConnectionIO]
    else create(user)
  } yield theUser

  def delete(id: Long): ConnectionIO[Boolean] =
    Queries.delete(id)

  def deleteAll: ConnectionIO[Int] =
    Queries.deleteAll.run

  def update(user: User): ConnectionIO[Option[User]] = for {
    _ ← Queries.update(user).run
    maybeUser ← getById(user.id)
  } yield maybeUser
}
