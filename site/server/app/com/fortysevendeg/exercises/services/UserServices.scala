package com.fortysevendeg.exercises.services

import scala.language.implicitConversions

import cats.data.Xor
import doobie.imports._
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scala.concurrent.{ Future, ExecutionContext }
import com.fortysevendeg.exercises.models.{ UserDoobieStore }
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
  ): Throwable Xor User

  def createUser(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): Throwable Xor User

  def update(
    id:         Int,
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): Boolean

  def delete(id: Int): Boolean
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
  ): Throwable Xor User = {
    (for {
      maybeUser ← UserDoobieStore.getByLogin(login)
      theUser ← if (maybeUser.isDefined) maybeUser.point[ConnectionIO] else UserDoobieStore.create(
        login,
        name,
        githubId,
        pictureUrl,
        githubUrl,
        email
      )
    } yield theUser.get).transact(transactor).attempt.run
  }

  def createUser(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): Throwable Xor User =
    UserDoobieStore.create(
      login,
      name,
      githubId,
      pictureUrl,
      githubUrl,
      email
    ).map(_.get).transact(transactor).attempt.run

  def update(
    id:         Int,
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): Boolean =
    UserDoobieStore.update(
      id,
      login,
      name,
      githubId,
      pictureUrl,
      githubUrl,
      email
    ).map(_.isDefined).transact(transactor).run

  def delete(id: Int): Boolean =
    UserDoobieStore.delete(id).transact(transactor).run

}

object UserServices {
  implicit def instance(
    implicit
    transactor: Transactor[Task]
  ): UserServices = new UserServiceImpl
}
