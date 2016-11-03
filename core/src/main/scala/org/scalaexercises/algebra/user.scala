package org.scalaexercises.algebra.user

import org.scalaexercises.types.user.{ User, UserCreation }

import cats.free._
import cats.implicits._
import io.freestyle._

/** Exposes User operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
@free trait UserOps[F[_]] {
  def getUsers: Free[F, List[User]]
  def getUserByLogin(login: String): Free[F, Option[User]]
  def createUser(user: UserCreation.Request): Free[F, UserCreation.Response]
  def updateUser(user: User): Free[F, Boolean]
  def deleteUser(user: User): Free[F, Boolean]
  def getOrCreate(user: UserCreation.Request): Free[F, UserCreation.Response] = for {
    maybeUser ← getUserByLogin(user.login)
    theUser ← maybeUser.fold(createUser(user))((user: User) ⇒ Free.pure(Either.right(user)))
  } yield theUser
}
