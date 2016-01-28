package com.fortysevendeg.exercises.models

import shared.User
import com.fortysevendeg.exercises.services.persistence.Persistence

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
  val TABLE = "users"
  val ALL_FIELDS = "id, login, name, github_id, picture_url, github_url, email"

  def queryAll: ConnectionIO[List[User]] =
    Persistence.fetchList[User](s"""
SELECT $ALL_FIELDS
FROM $TABLE
""")

  def all: List[User] = {
    queryAll transact (transactor) run
  }

  def queryByLogin(login: String): ConnectionIO[Option[User]] =
    Persistence.fetchOption[String, User](s"""
SELECT $ALL_FIELDS
FROM $TABLE
WHERE login = ?
""", login)

  def getByLogin(login: String): Option[User] = {
    queryByLogin(login).transact(transactor).run
  }

  def queryById(id: Int): ConnectionIO[Option[User]] = {
    Persistence.fetchOption[Int, User](s"""
SELECT $ALL_FIELDS
FROM $TABLE
WHERE id = ?""", id)
  }

  def getById(id: Int): Option[User] = {
    queryById(id).transact(transactor).run
  }

  def insertQuery(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): ConnectionIO[Option[User]] = {
    Persistence.update(s"""
INSERT INTO $TABLE (login, name, github_id, picture_url, github_url, email)
            VALUES ($login, $name, $github_id, $picture_url, $github_url, $email)
""").flatMap(queryById)
  }

  def create(user: User): Option[User] = {
    insertQuery(
      user.login,
      user.name,
      user.github_id,
      user.picture_url,
      user.github_url,
      user.email
    ).transact(transactor).run
  }

  def deleteQuery(user: User): ConnectionIO[Boolean] = {
    queryByLogin(user.login).flatMap(maybeUser ⇒ {
      maybeUser match {
        case None ⇒ sql"select false".query[Boolean].unique
        case Some(_) ⇒ Persistence.update(s"""
DELETE FROM $TABLE
      WHERE login = $user.login
""").map(_ ⇒ true)
      }
    })
  }

  def delete(user: User): Boolean =
    deleteQuery(user).transact(transactor).run

  def updateQuery(user: User): ConnectionIO[Int] = {
    Persistence.update(s"""
UPDATE users
SET    name = $user.name,
       github_id = $user.github_id,
       picture_url = $user.picture_url,
       github_url = $user.github_url,
       email = $user.email
WHERE id = $user.id;
""")
  }

  def updateIfPresentQuery(user: User): ConnectionIO[Option[User]] = {
    queryByLogin(user.login).flatMap({
      case Some(_) ⇒ updateQuery(user).flatMap(queryById)
      case _       ⇒ queryByLogin(user.login)
    })
  }

  def update(user: User): Option[User] =
    updateIfPresentQuery(user).transact(transactor).run
}

object UserStore {
  implicit def instance(implicit transactor: Transactor[Task]): UserStore = new UserDoobieStore
}
