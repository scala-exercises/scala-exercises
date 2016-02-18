/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.services.free

import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress
import com.fortysevendeg.exercises.persistence.repositories.UserProgressRepository
import com.fortysevendeg.shared.free.ExerciseOps
import doobie.imports.Transactor
import shared._

import cats.free.Free
import cats.free.Inject

import scalaz.concurrent.Task

/** Users Progress Ops GADT
  */
sealed trait UserProgressOp[A]

final case class UpdateUserProgress(
  userProgress: SaveUserProgress.Request
) extends UserProgressOp[UserProgress]

/** Exposes User Progress operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class UserProgressOps[F[_]](implicit I: Inject[UserProgressOp, F], EO: ExerciseOps[F], UPR: UserProgressRepository, DBO: DBOps[F], T: Transactor[Task]) {

  def saveUserProgress(userProgress: SaveUserProgress.Request): Free[F, UserProgress] =
    Free.inject[UserProgressOp, F](UpdateUserProgress(userProgress))

  def fetchUserProgress(user: User): Free[F, OverallUserProgress] = {
    import ConnectionIOOps._
    for {
      lbs ← UPR.findByUserIdAggregated(user.id).liftF[F]
      items = lbs map {
        case (libraryName, sections, succeeded) ⇒ OverallUserProgressItem(libraryName, sections, succeeded)
      }
    } yield OverallUserProgress(items)
  }

  def fetchUserProgressByLibrary(user: User, libraryName: String): Free[F, LibrarySections] = {
    import ConnectionIOOps._
    UPR.findUserProgressByLibrary(user, libraryName).liftF[F] map { ss ⇒
      LibrarySections(libraryName, ss)
    }
  }

  def fetchUserProgressByLibrarySection(
    user:        User,
    libraryName: String,
    sectionName: String
  ): Free[F, LibrarySectionArgs] = {
    import ConnectionIOOps._
    for {
      lbs ← UPR.findUserProgressBySection(user, libraryName, sectionName).liftF[F]
      sectionCount ← EO.getLibrary(libraryName) map (_ map (_.sections.size) getOrElse 0)
    } yield LibrarySectionArgs(libraryName, sectionCount, lbs.exerciseList, lbs.succeeded)
  }
}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object UserProgressOps {

  implicit def instance[F[_]](implicit I: Inject[UserProgressOp, F], EO: ExerciseOps[F], UPR: UserProgressRepository, DBO: DBOps[F], T: Transactor[Task]): UserProgressOps[F] = new UserProgressOps[F]

}
