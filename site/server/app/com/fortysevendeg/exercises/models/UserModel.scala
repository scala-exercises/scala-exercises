package com.fortysevendeg.exercises.models

import shared.User

import com.fortysevendeg.exercises.models.queries.Queries

import doobie.imports._
import scalaz.concurrent.Task

trait UserStore {
  def all: ConnectionIO[List[User]]

  def getByLogin(login: String): ConnectionIO[Option[User]]

  def getById(id: Long): ConnectionIO[Option[User]]

  def create(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): ConnectionIO[Option[User]]

  def delete(id: Long): ConnectionIO[Boolean]

  def update(
    id:         Long,
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): ConnectionIO[Option[User]]
}

object UserDoobieStore extends UserStore {
  def all: ConnectionIO[List[User]] = {
    Queries.all.list
  }

  def getByLogin(login: String): ConnectionIO[Option[User]] = {
    Queries.byLogin(login).option
  }

  def getById(id: Long): ConnectionIO[Option[User]] = {
    Queries.byId(id).option
  }

  def create(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): ConnectionIO[Option[User]] = for {
    _ ← Queries.insert(login, name, githubId, pictureUrl, githubUrl, email).run
    user ← getByLogin(login)
  } yield user

  def deleteQuery(login: String): ConnectionIO[Boolean] = {
    Queries.byLogin(login).option.flatMap({
      case None ⇒ sql"select false".query[Boolean].unique
      case Some(u) ⇒ Queries.update(
        u.id.get,
        u.login,
        u.name,
        u.githubId,
        u.pictureUrl,
        u.githubUrl,
        u.email
      ).run.map(_ ⇒ true)
    })
  }

  def delete(id: Long): ConnectionIO[Boolean] =
    Queries.delete(id)

  def deleteAll: ConnectionIO[Int] =
    Queries.deleteAll.run

  def updateQuery(user: User): ConnectionIO[Int] = {
    require(user.id.isDefined, "Can only update existing users")

    Queries.update(
      user.id.get,
      user.login,
      user.name,
      user.githubId,
      user.pictureUrl,
      user.githubUrl,
      user.email
    ).run
  }

  def update(
    id:         Long,
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): ConnectionIO[Option[User]] = for {
    _ ← Queries.update(
      id,
      login,
      name,
      githubId,
      pictureUrl,
      githubUrl,
      email
    ).run
    maybeUser ← getById(id)
  } yield maybeUser
}
