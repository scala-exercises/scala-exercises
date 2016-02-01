package com.fortysevendeg.exercises.models

import shared.User
import com.fortysevendeg.exercises.services.persistence.Persistence
import com.fortysevendeg.exercises.models.queries.Queries

import doobie.imports._
import scalaz.concurrent.Task

trait UserStore {
  def all: ConnectionIO[List[User]]

  def getByLogin(login: String): ConnectionIO[Option[User]]

  def getById(id: Int): ConnectionIO[Option[User]]

  def create(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): ConnectionIO[Option[User]]

  def delete(id: Int): ConnectionIO[Boolean]

  def update(
    id:          Int,
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): ConnectionIO[Option[User]]
}

object UserDoobieStore extends UserStore {
  def all: ConnectionIO[List[User]] = {
    Queries.all.list
  }

  def getByLogin(login: String): ConnectionIO[Option[User]] = {
    Queries.byLogin(login).option
  }

  def getById(id: Int): ConnectionIO[Option[User]] = {
    Queries.byId(id).option
  }

  def create(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): ConnectionIO[Option[User]] = for {
    _ ← Queries.insert(login, name, github_id, picture_url, github_url, email).run
    user ← getByLogin(login)
  } yield user

  def deleteQuery(login: String): ConnectionIO[Boolean] = {
    Queries.byLogin(login).option.flatMap({
      case None ⇒ sql"select false".query[Boolean].unique
      case Some(u) ⇒ Queries.update(
        u.id.get,
        u.login,
        u.name,
        u.github_id,
        u.picture_url,
        u.github_url,
        u.email
      ).run.map(_ ⇒ true)
    })
  }

  def delete(id: Int): ConnectionIO[Boolean] =
    Queries.delete(id)

  def deleteAll: ConnectionIO[Int] =
    Queries.deleteAll.run

  def updateQuery(user: User): ConnectionIO[Int] = {
    require(user.id.isDefined, "Can only update existing users")

    Queries.update(
      user.id.get,
      user.login,
      user.name,
      user.github_id,
      user.picture_url,
      user.github_url,
      user.email
    ).run
  }

  def update(
    id:          Int,
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): ConnectionIO[Option[User]] = for {
    _ ← Queries.update(
      id,
      login,
      name,
      github_id,
      picture_url,
      github_url,
      email
    ).run
    maybeUser ← getById(id)
  } yield maybeUser
}
