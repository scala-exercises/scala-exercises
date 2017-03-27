/*
 * scala-exercises - core
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.algebra.user

import org.scalaexercises.types.user.{ User, UserCreation }

import cats.Applicative
import cats.implicits._
import cats.free._
import freestyle._
import freestyle.implicits._

/** Exposes User operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
@free trait UserOps[F[_]] {
  def getUsers: FreeS[F, List[User]]
  def getUserByLogin(login: String): FreeS[F, Option[User]]
  def createUser(user: UserCreation.Request): FreeS[F, UserCreation.Response]
  def updateUser(user: User): FreeS[F, Boolean]
  def deleteUser(user: User): FreeS[F, Boolean]
  def getOrCreate(user: UserCreation.Request): FreeS[F, UserCreation.Response] = for {
    maybeUser ← getUserByLogin(user.login)
    theUser ← maybeUser.fold(createUser(user))(
      (user: User) ⇒ Free.liftF[FreeApplicative[F, ?], UserCreation.Response](
        FreeApplicative.pure(
          Either.right(user): UserCreation.Response
        )
      )
    )
  } yield theUser
}
