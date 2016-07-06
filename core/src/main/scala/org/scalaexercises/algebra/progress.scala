/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.algebra.progress

import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.types.user._
import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._

import cats.Monad
import cats.std.list._
import cats.free._

/** Users Progress Ops GADT
  */
sealed trait UserProgressOp[A]

final case class GetLastSeenSection(
  user:    User,
  library: String
) extends UserProgressOp[Option[String]]

final case class GetExerciseEvaluations(
  user:    User,
  library: String,
  section: String
) extends UserProgressOp[List[UserProgress]]

final case class UpdateUserProgress(
  userProgress: SaveUserProgress.Request
) extends UserProgressOp[UserProgress]

/** Exposes User Progress operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class UserProgressOps[F[_]](
    implicit
    I:  Inject[UserProgressOp, F],
    EO: ExerciseOps[F],
    FM: Monad[Free[F, ?]]
) {
  def saveUserProgress(userProgress: SaveUserProgress.Request): Free[F, UserProgress] =
    Free.inject[UserProgressOp, F](UpdateUserProgress(userProgress))

  def getExerciseEvaluations(user: User, library: String, section: String): Free[F, List[UserProgress]] =
    Free.inject[UserProgressOp, F](GetExerciseEvaluations(user, library, section))

  def getLastSeenSection(user: User, library: String): Free[F, Option[String]] =
    Free.inject[UserProgressOp, F](GetLastSeenSection(user, library))

  def getSolvedExerciseCount(user: User, library: String, section: String): Free[F, Int] = for {
    tried ← getExerciseEvaluations(user, library, section)
  } yield tried.filter(_.succeeded).size

  def fetchMaybeUserProgress(user: Option[User]): Free[F, OverallUserProgress] = {
    user.fold(anonymousUserProgress)(fetchUserProgress)
  }

  private[this] def anonymousUserProgress: Free[F, OverallUserProgress] = for {
    libraries ← EO.getLibraries
    libs = libraries.map(l ⇒ {
      OverallUserProgressItem(
        libraryName = l.name,
        completedSections = 0,
        totalSections = l.sections.size
      )
    })
  } yield OverallUserProgress(libraries = libs)

  def getCompletedSectionCount(user: User, library: Library): Free[F, Int] = {
    for {
      finishedSections ← FM.sequence(
        library.sections.map(s ⇒ {
          for {
            solvedExercises ← getSolvedExerciseCount(user, library.name, s.name)
          } yield solvedExercises == s.exercises.size
        })
      )
    } yield finishedSections.filter(_ == true).size
  }

  def fetchUserProgress(user: User): Free[F, OverallUserProgress] = {
    for {
      allLibraries ← EO.getLibraries
      libraryProgress ← FM.sequence(allLibraries.map(l ⇒ {
        for {
          completedSections ← getCompletedSectionCount(user, l)
        } yield OverallUserProgressItem(
          libraryName = l.name,
          completedSections = completedSections,
          totalSections = l.sections.size
        )
      }))
    } yield OverallUserProgress(
      libraries = libraryProgress
    )
  }

  def fetchMaybeUserProgressByLibrary(user: Option[User], libraryName: String): Free[F, LibraryProgress] = {
    user.fold(anonymousUserProgressByLibrary(libraryName))(fetchUserProgressByLibrary(_, libraryName))
  }

  private[this] def anonymousUserProgressByLibrary(libraryName: String): Free[F, LibraryProgress] = {
    for {
      lib ← EO.getLibrary(libraryName)
      sections = lib.fold(Nil: List[SectionProgress])(l ⇒ {
        l.sections.map(s ⇒
          SectionProgress(
            sectionName = s.name,
            succeeded = false
          ))
      })
    } yield LibraryProgress(
      libraryName = libraryName,
      sections = sections
    )
  }

  def fetchUserProgressByLibrary(user: User, libraryName: String): Free[F, LibraryProgress] = {
    for {
      maybeLib ← EO.getLibrary(libraryName)
      libSections = maybeLib.fold(Nil: List[Section])(_.sections)
      sectionProgress ← FM.sequence(
        libSections.map(s ⇒ {
          for {
            solvedExercises ← getSolvedExerciseCount(user, libraryName, s.name)
          } yield SectionProgress(
            sectionName = s.name,
            succeeded = s.exercises.size == solvedExercises
          )
        })
      )
    } yield LibraryProgress(
      libraryName = libraryName,
      sections = sectionProgress
    )
  }

  def fetchUserProgressByLibrarySection(
    user:        User,
    libraryName: String,
    sectionName: String
  ): Free[F, SectionExercises] = {
    for {
      maybeSection ← EO.getSection(libraryName, sectionName)
      evaluations ← getExerciseEvaluations(user, libraryName, sectionName)
      exercises = maybeSection.fold(Nil: List[Exercise])(_.exercises).map(ex ⇒ {
        val maybeEvaluation = evaluations.find(_.method == ex.method)
        ExerciseProgress(
          methodName = ex.method,
          args = maybeEvaluation.fold(Nil: List[String])(_.args),
          succeeded = maybeEvaluation.fold(false: Boolean)(_.succeeded)
        )
      })
      totalExercises = maybeSection.fold(0)(_.exercises.size)
    } yield SectionExercises(
      libraryName = libraryName,
      sectionName = sectionName,
      exercises = exercises,
      totalExercises = totalExercises
    )
  }
}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object UserProgressOps {

  implicit def instance[F[_]](
    implicit
    I:  Inject[UserProgressOp, F],
    EO: ExerciseOps[F],
    FM: Monad[Free[F, ?]]
  ): UserProgressOps[F] = new UserProgressOps[F]

}
