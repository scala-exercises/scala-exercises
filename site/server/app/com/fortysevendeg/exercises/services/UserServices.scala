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
  ): Throwable Xor User

  def createUser(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Throwable Xor User

  def update(
    id:          Int,
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Boolean

  def delete(id: Int): Boolean
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
  ): Throwable Xor User = {
    getUserByLogin(login) match {
      case Some(user) ⇒ Xor.Right(user)
      case _          ⇒ createUser(login, name, github_id, picture_url, github_url, email)
    }
  }

  def createUser(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Throwable Xor User = {
    Xor.fromOption(userStore.create(
      login,
      name,
      github_id,
      picture_url,
      github_url,
      email
    ), new Exception("Couldn't create user"))
  }

  def update(
    id:          Int,
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Boolean =
    userStore.update(
      id,
      login,
      name,
      github_id,
      picture_url,
      github_url,
      email
    ).isDefined

  def delete(id: Int): Boolean =
    userStore.delete(id)
}

object UserServices {
  implicit def instance(implicit userStore: UserStore): UserServices = new UserServiceImpl
}
