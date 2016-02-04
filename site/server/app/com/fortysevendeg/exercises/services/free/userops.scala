package com.fortysevendeg.exercises.services.free

import scala.language.higherKinds

import cats.data.Xor
import cats.free.Free
import cats.free.Inject

import shared.User
import com.fortysevendeg.exercises.models.UserCreation

/** Exercise Ops GADT
  */
sealed trait UserOp[A]
final case class GetUsers() extends UserOp[List[User]]
final case class GetUserByLogin(login: String) extends UserOp[Option[User]]
final case class CreateUser(user: UserCreation.Request) extends UserOp[UserCreation.Response]
final case class UpdateUser(user: User) extends UserOp[Boolean]
final case class DeleteUser(user: User) extends UserOp[Boolean]

/** Exposes User operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class UserOps[F[_]](implicit I: Inject[UserOp, F]) {
  def getUsers: Free[F, List[User]] =
    Free.inject[UserOp, F](GetUsers())

  def getUserByLogin(login: String): Free[F, Option[User]] =
    Free.inject[UserOp, F](GetUserByLogin(login))

  def createUser(user: UserCreation.Request): Free[F, UserCreation.Response] =
    Free.inject[UserOp, F](CreateUser(user))

  def updateUser(user: User): Free[F, Boolean] =
    Free.inject[UserOp, F](UpdateUser(user))

  def getOrCreateUser(
    login:     String,
    name:      String,
    githubId:  String,
    avatarUrl: String,
    htmlUrl:   String,
    email:     String
  ): Free[F, UserCreation.Response] = for {
    maybeUser ← getUserByLogin(login)
    //    theUser <- if (maybeUser.isDefined) Xor.Right(maybeUser.get) else createUser()
  } yield ???

  def deleteUser(user: User): Free[F, Boolean] =
    Free.inject[UserOp, F](DeleteUser(user))
}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object UserOps {

  implicit def instance[F[_]](implicit I: Inject[UserOp, F]): UserOps[F] = new UserOps[F]

}

