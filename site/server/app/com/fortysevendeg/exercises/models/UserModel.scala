package com.fortysevendeg.exercises.models

import shared.User
import com.fortysevendeg.exercises.services.persistence.Persistence

import doobie.imports._
import scalaz.concurrent.Task

trait UserStore {
  def all: List[User]

  def getByLogin(login: String): Option[User]

  def create(user: User): Option[User]

  // TODO: update, delete
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

  def insert(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): ConnectionIO[Option[User]] = {
    Persistence.update("""
INSERT INTO users (login, name, github_id, picture_url, github_url, email)
           VALUES ($login, $name, $github_id, $picture_url, $github_url, $email)
""").flatMap(queryById)
  }

  def create(user: User): Option[User] = {
    insert(
      user.login,
      user.name,
      user.github_id,
      user.picture_url,
      user.github_url,
      user.email
    ).transact(transactor).run
  }
}

object UserStore {
  implicit def instance(implicit transactor: Transactor[Task]): UserStore = new UserDoobieStore
}
