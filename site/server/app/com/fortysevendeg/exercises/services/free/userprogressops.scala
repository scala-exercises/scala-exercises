/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.services.free

import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress
import shared._

import cats.free.Free
import cats.free.Inject

/** Users Progress Ops GADT
  */
sealed trait UserProgressOp[A]
final case class UpdateUserProgress(
  userProgress: SaveUserProgress.Request
) extends UserProgressOp[UserProgress]
final case class FetchUserProgress(user: User) extends UserProgressOp[OverallUserProgress]
final case class FetchUserProgressByLibrary(
  user:        User,
  libraryName: String
) extends UserProgressOp[LibrarySections]
final case class FetchUserProgressByLibrarySection(
  user:        User,
  libraryName: String,
  sectionName: String
) extends UserProgressOp[LibrarySectionArgs]

/** Exposes User Progress operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class UserProgressOps[F[_]](implicit I: Inject[UserProgressOp, F]) {

  def saveUserProgress(userProgress: SaveUserProgress.Request): Free[F, UserProgress] =
    Free.inject[UserProgressOp, F](UpdateUserProgress(userProgress))

  def fetchUserProgress(user: User): Free[F, OverallUserProgress] =
    Free.inject[UserProgressOp, F](FetchUserProgress(user))

  def fetchUserProgressByLibrary(user: User, libraryName: String): Free[F, LibrarySections] =
    Free.inject[UserProgressOp, F](FetchUserProgressByLibrary(user, libraryName))

  def fetchUserProgressByLibrarySection(user: User, libraryName: String, sectionName: String): Free[F, LibrarySectionArgs] =
    Free.inject[UserProgressOp, F](FetchUserProgressByLibrarySection(user, libraryName, sectionName))
}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object UserProgressOps {

  implicit def instance[F[_]](implicit I: Inject[UserProgressOp, F]): UserProgressOps[F] = new UserProgressOps[F]

}
