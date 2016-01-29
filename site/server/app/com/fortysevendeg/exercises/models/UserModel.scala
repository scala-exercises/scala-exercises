package com.fortysevendeg.exercises.models

import shared.User
import com.fortysevendeg.exercises.services.persistence.Persistence
import com.fortysevendeg.exercises.models.queries.Queries

import doobie.imports._
import scalaz.concurrent.Task

trait UserStore {
  def all: List[User]

  def getByLogin(login: String): Option[User]

  def create(user: User): Option[User]

  def delete(user: User): Boolean

  def update(user: User): Option[User]
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

  def create(user: User): Option[User] = {
    user.id match {
      case Some(id) ⇒ (for {
        _ ← Queries.insert(id, user.login, user.name, user.github_id, user.picture_url, user.github_url, user.email).run
        user ← Queries.byId(id).option
      } yield user).transact(transactor).run
      case None ⇒ None
    }
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

  def delete(user: User): Boolean =
    deleteQuery(user.login).transact(transactor).run

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

  def update(user: User): Option[User] =
    updateQuery(user).transact(transactor).map(_ ⇒ Some(user)).run
}

object UserStore {
  implicit def instance(implicit transactor: Transactor[Task]): UserStore = new UserDoobieStore
}
