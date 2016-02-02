package com.fortysevendeg.exercises.services.free

import scala.language.higherKinds

import cats.data.Xor
import cats.free.Free
import cats.free.Inject

import shared.User

/** Exercise Ops GADT
  */
sealed trait UserOp[A]
final case class GetUsers() extends UserOp[List[User]]
final case class GetUserByLogin(login: String) extends UserOp[Option[User]]
final case class CreateUser(
  login:      String,
  name:       String,
  githubId:   String,
  pictureUrl: String,
  githubUrl:  String,
  email:      String
) extends UserOp[Throwable Xor User]
final case class UpdateUser(
  id:         Int,
  login:      String,
  name:       String,
  githubId:   String,
  pictureUrl: String,
  githubUrl:  String,
  email:      String
) extends UserOp[Boolean]
final case class DeleteUser(id: Int) extends UserOp[Boolean]

/** Exposes User operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class UserOps[F[_]](implicit I: Inject[UserOp, F]) {
  def getUsers: Free[F, List[User]] =
    Free.inject[UserOp, F](GetUsers())

  def getUserByLogin(login: String): Free[F, Option[User]] =
    Free.inject[UserOp, F](GetUserByLogin(login))

  def createUser(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): Free[F, Throwable Xor User] =
    Free.inject[UserOp, F](CreateUser(login, name, githubId, pictureUrl, githubUrl, email))

  def updateUser(
    id:         Int,
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ): Free[F, Boolean] =
    Free.inject[UserOp, F](UpdateUser(
      id: Int,
      login: String,
      name: String,
      githubId: String,
      pictureUrl: String,
      githubUrl: String,
      email: String
    ))

  def deleteUser(id: Int): Free[F, Boolean] =
    Free.inject[UserOp, F](DeleteUser(id))
}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object UserOps {

  implicit def instance[F[_]](implicit I: Inject[UserOp, F]): UserOps[F] = new UserOps[F]

}

