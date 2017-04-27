/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.scalaexercises.algebra.progress

import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.types.user._
import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._

import cats.{Applicative, Monad, Unapply}
import cats.free._
import cats.implicits._

import freestyle._
import freestyle.implicits._

/** Exposes User Progress operations as a Free monadic algebra that may be combined with other Algebras via
 * Coproduct
 */
@free
trait UserProgressOps {
  def saveUserProgress(userProgress: SaveUserProgress.Request): FS[UserProgress]

  def getExerciseEvaluations(
      user: User,
      library: String,
      section: String): FS[List[UserProgress]]

  def getLastSeenSection(user: User, library: String): FS[Option[String]]

  def getSolvedExerciseCount(user: User, library: String, section: String): FS[Int] =
    getExerciseEvaluations(user, library, section).map(tried ⇒ tried.count(_.succeeded))

  def isSectionCompleted(
      user: User,
      libraryName: String,
      section: Section): FS[Boolean] =
    getSolvedExerciseCount(user, libraryName, section.name).map(solvedExercises ⇒
      solvedExercises == section.exercises.size)

}

class UserExercisesProgress[F[_]](implicit UO: UserProgressOps[F], EO: ExerciseOps[F]) {

  def fetchMaybeUserProgress(user: Option[User]): FreeS[F, OverallUserProgress] =
    user.fold(anonymousUserProgress)(fetchUserProgress)

  private[this] def anonymousUserProgress: FreeS[F, OverallUserProgress] =
    for {
      libraries ← EO.getLibraries
      libs = libraries.map(l ⇒ {
        OverallUserProgressItem(
          libraryName = l.name,
          completedSections = 0,
          totalSections = l.sections.size
        )
      })
    } yield OverallUserProgress(libraries = libs)

  def getCompletedSectionCount(user: User, library: Library): FreeS[F, Int] =
    library.sections
      .traverse[FreeS[F, ?], Boolean](UO.isSectionCompleted(user, library.name, _))
      .map(
        _.count(identity)
      )

  def fetchUserProgress(user: User): FreeS[F, OverallUserProgress] = {
    def getLibraryProgress(library: Library): FreeS[F, OverallUserProgressItem] =
      getCompletedSectionCount(user, library).map { completedSections ⇒
        OverallUserProgressItem(
          libraryName = library.name,
          completedSections = completedSections,
          totalSections = library.sections.size
        )
      }

    for {
      allLibraries ← EO.getLibraries
      libraryProgress ← allLibraries.traverse[FreeS[F, ?], OverallUserProgressItem](
        getLibraryProgress)
    } yield OverallUserProgress(libraries = libraryProgress)
  }

  def fetchMaybeUserProgressByLibrary(user: Option[User], libraryName: String): FreeS[F, LibraryProgress] =
    user.fold(anonymousUserProgressByLibrary(libraryName))(
      fetchUserProgressByLibrary(_, libraryName))

  private[this] def anonymousUserProgressByLibrary(libraryName: String): FreeS[F, LibraryProgress] = {
    for {
      lib ← EO.getLibrary(libraryName)
      sections = lib.foldMap(
        _.sections.map(
          s ⇒
            SectionProgress(
              sectionName = s.name,
              succeeded = false
            )))
    } yield
      LibraryProgress(
        libraryName = libraryName,
        sections = sections
      )
  }

  def fetchUserProgressByLibrary(user: User, libraryName: String): FreeS[F, LibraryProgress] = {
    def getSectionProgress(section: Section): FreeS[F, SectionProgress] =
      UO.isSectionCompleted(user, libraryName, section).map { completed ⇒
        SectionProgress(
          sectionName = section.name,
          succeeded = completed
        )
      }

    for {
      maybeLib ← EO.getLibrary(libraryName)
      libSections = maybeLib.foldMap(_.sections)
      sectionProgress ← libSections.traverse[FreeS[F, ?], SectionProgress](getSectionProgress)
    } yield
      LibraryProgress(
        libraryName = libraryName,
        sections = sectionProgress
      )
  }

  def fetchUserProgressByLibrarySection(
                                         user: User,
                                         libraryName: String,
                                         sectionName: String
                                       ): FreeS[F, SectionExercises] = {
    for {
      maybeSection ← EO.getSection(libraryName, sectionName)
      evaluations  ← UO.getExerciseEvaluations(user, libraryName, sectionName)
      sectionExercises = maybeSection.foldMap(_.exercises)
      exercises = sectionExercises.map(ex ⇒ {
        val maybeEvaluation = evaluations.find(_.method == ex.method)
        ExerciseProgress(
          methodName = ex.method,
          args = maybeEvaluation.foldMap(_.args),
          succeeded = maybeEvaluation.fold(false)(_.succeeded)
        )
      })
      totalExercises = sectionExercises.size
    } yield
      SectionExercises(
        libraryName = libraryName,
        sectionName = sectionName,
        exercises = exercises,
        totalExercises = totalExercises
      )
  }

}

object UserExercisesProgress {
  implicit def instance [F[_]](implicit UO: UserProgressOps[F], EO: ExerciseOps[F]): UserExercisesProgress[F] = new UserExercisesProgress[F]

}
