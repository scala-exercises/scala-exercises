package com.fortysevendeg.exercises.models

import doobie.imports._

import shared.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.concurrent.Task

import com.fortysevendeg.exercises.services.persistence.Persistence

trait UserStore {
  def all: List[User]

  def getByLogin(login: String): Option[User]
}

class UserDoobieStore(implicit transactor: Transactor[Task]) extends UserStore {
  def all: List[User] = {
    val query = Persistence.fetchList[User](
      "select id, login, name, github_id, picture_url, github_url, email from users"
    )
    query.transact(transactor).run
  }

  def getByLogin(login: String): Option[User] = {
    val query = Persistence.fetchOption[String, User](
      "select id, login, name, github_id, picture_url, github_url, email from users where login = ?",
      login
    )
    query.transact(transactor).run
  }

}

object UserStore {
  implicit def instance(implicit transactor: Transactor[Task]): UserStore = new UserDoobieStore
}
