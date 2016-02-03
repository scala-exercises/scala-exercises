package com.fortysevendeg.exercises.services

import scala.language.implicitConversions

import cats.data.Xor
import doobie.imports._
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scala.concurrent.{ Future, ExecutionContext }
import com.fortysevendeg.exercises.models.{ UserDoobieStore, UserCreation }
import shared.User

trait UserServices {
  def all: List[User]

  def getUserByLogin(login: String): Option[User]

  def getUserOrCreate(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): UserCreation.Response

  def createUser(user: NewUser): Throwable Xor User

  def update(user: User): Boolean

  def delete(id: Long): Boolean
}

class UserServiceImpl(
    implicit
    transactor: Transactor[Task]
) extends UserServices {

  implicit def scalazToCats[A, B](disj: \/[A, B]): Xor[A, B] = disj match {
    case -\/(left)  ⇒ Xor.Left(left)
    case \/-(right) ⇒ Xor.Right(right)
  }

  def all: List[User] =
    UserDoobieStore.all transact (transactor) run

  def getUserByLogin(login: String): Option[User] =
    UserDoobieStore.getByLogin(login) transact (transactor) run

  def getUserOrCreate(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): UserCreation.Response = {
    UserDoobieStore.getOrCreate(
      UserCreation.Request(
        login,
        name,
        githubId,
        pictureUrl,
        githubUrl,
        email
      )
    ).transact(transactor).run
  }

  def createUser(user: NewUser): Throwable Xor User =
    UserDoobieStore.create(user).map(_.get).transact(transactor).attempt.run

  def update(user: User): Boolean =
    UserDoobieStore.update(user).map(_.isDefined).transact(transactor).run

  def delete(id: Long): Boolean =
    UserDoobieStore.delete(id).transact(transactor).run

}

object UserServices {
  implicit def instance(
    implicit
    transactor: Transactor[Task]
  ): UserServices = new UserServiceImpl
}
