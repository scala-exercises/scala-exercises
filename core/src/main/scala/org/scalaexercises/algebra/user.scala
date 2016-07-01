package org.scalaexercises.algebra.user

import org.scalaexercises.types.user.{ User, UserCreation }

import cats.data.Xor
import cats.free._

/** Users Ops GADT
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

  def deleteUser(user: User): Free[F, Boolean] =
    Free.inject[UserOp, F](DeleteUser(user))

  def getOrCreate(user: UserCreation.Request): Free[F, UserCreation.Response] = for {
    maybeUser ← getUserByLogin(user.login)
    theUser ← maybeUser.fold(createUser(user))((user: User) ⇒ Free.pure(Xor.Right(user)))
  } yield theUser
}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object UserOps {

  implicit def instance[F[_]](implicit I: Inject[UserOp, F]): UserOps[F] = new UserOps[F]

}
