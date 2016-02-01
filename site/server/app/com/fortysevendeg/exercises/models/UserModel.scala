package com.fortysevendeg.exercises.models

import shared.User
import com.fortysevendeg.exercises.services.persistence.Persistence
import com.fortysevendeg.exercises.models.queries.Queries

import doobie.imports._
import scalaz.concurrent.Task

trait UserStore {
  def all: List[User]

  def getByLogin(login: String): Option[User]

  def getById(id: Int): Option[User]

  def create(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Option[User]

  def delete(id: Int): Boolean

  def update(
    id:          Int,
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Option[User]
}

class UserDoobieStore(implicit transactor: Transactor[Task]) extends UserStore {
  def all: List[User] = {
    Queries.all.list transact (transactor) run
  }

  def getByLogin(login: String): Option[User] = {
    Queries.byLogin(login).option.transact(transactor).run
  }

  def getById(id: Int): Option[User] = {
    Queries.byId(id).option.transact(transactor).run
  }

  // TODO: beware of the constraint violations
  def create(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Option[User] = {
    (for {
      _ ← Queries.insert(login, name, github_id, picture_url, github_url, email).run
      user ← Queries.byLogin(login).option
    } yield user).transact(transactor).run
  }

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

  def delete(id: Int): Boolean =
    Queries.delete(id) transact (transactor) run

  def deleteAll =
    Queries.deleteAll.run transact (transactor) run

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
  ): Option[User] = {
    Queries.update(
      id,
      login,
      name,
      github_id,
      picture_url,
      github_url,
      email
    ).run.flatMap(_ ⇒ Queries.byId(id).option) transact (transactor) run
  }
}

object UserStore {
  implicit def instance(implicit transactor: Transactor[Task]): UserStore = new UserDoobieStore
}
