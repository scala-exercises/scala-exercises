package com.fortysevendeg.exercises.models

import shared.User

import com.fortysevendeg.exercises.models.queries.Queries

import doobie.imports._
import scalaz.concurrent.Task

case class NewUser(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
) {

  def withId(id: Long): User =
    User(id, login, name, githubId, pictureUrl, githubUrl, email)
}

trait UserStore {
  def all: ConnectionIO[List[User]]

  def getByLogin(login: String): ConnectionIO[Option[User]]

  def getById(id: Long): ConnectionIO[Option[User]]

  def create(user: NewUser): ConnectionIO[Option[User]]

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

  def create(user: NewUser): ConnectionIO[Option[User]] = for {
    _ ← Queries.insert(user).run
    user ← getByLogin(user.login)
  } yield user

  def delete(id: Long): ConnectionIO[Boolean] =
    Queries.delete(id)

  def deleteAll: ConnectionIO[Int] =
    Queries.deleteAll.run

  def update(user: User): ConnectionIO[Option[User]] = for {
    _ ← Queries.update(user).run
    maybeUser ← getById(user.id)
  } yield maybeUser
}
