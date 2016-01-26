package com.fortysevendeg.exercises.services

import cats.data.Xor

import com.fortysevendeg.exercises.models.{ UserStore }
import com.fortysevendeg.exercises.services.messages._

import shared.User

import scala.concurrent.{ Future, ExecutionContext }

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
    None // TODO

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
    User(None, login, name, github_id, picture_url, github_url, email)

  def updateUser(user: User): Boolean =
    ???

  def deleteUser(user: User): Boolean =
    ???
}

object UserServices {
  implicit def instance(implicit userStore: UserStore): UserServices = new UserServiceImpl
}
