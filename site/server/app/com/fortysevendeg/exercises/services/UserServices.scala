package com.fortysevendeg.exercises.services

import cats.data.Xor
import scala.concurrent.{ Future, ExecutionContext }

import com.fortysevendeg.exercises.models.{ UserStore }
import shared.User

trait UserServices {
  def all: List[User]

  def getUserByLogin(login: String): Option[User]

  def getUserOrCreate(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): User
}

class UserServiceImpl(implicit userStore: UserStore) extends UserServices {
  def all: List[User] =
    userStore.all

  def getUserByLogin(login: String): Option[User] =
    userStore.getByLogin(login)

  def getUserOrCreate(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): User =
    getUserByLogin(login).getOrElse(createUser(login, name, github_id, picture_url, github_url, email))

  def createUser(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): User =
    ???

  def updateUser(user: User): Boolean =
    ???

  def deleteUser(user: User): Boolean =
    ???
}

object UserServices {
  implicit def instance(implicit userStore: UserStore): UserServices = new UserServiceImpl
}
