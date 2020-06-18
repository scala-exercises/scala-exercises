/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
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
 */

package org.scalaexercises.algebra.progress

import cats.Monad
import cats.implicits._
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._
import org.scalaexercises.types.user._

class UserExercisesProgress[F[_]: Monad](implicit UO: UserProgressOps[F], EO: ExerciseOps[F]) {

  def fetchMaybeUserProgress(user: Option[User]): F[OverallUserProgress] =
    user.fold(anonymousUserProgress)(fetchUserProgress)

  private[this] def anonymousUserProgress: F[OverallUserProgress] =
    for {
      libraries <- EO.getLibraries
      libs = libraries.map { l =>
        OverallUserProgressItem(
          libraryName = l.name,
          completedSections = 0,
          totalSections = l.sections.size
        )
      }
    } yield OverallUserProgress(libraries = libs)

  def getCompletedSectionCount(user: User, library: Library): F[Int] =
    library.sections
      .traverse(UO.isSectionCompleted(user, library.name, _))
      .map(
        _.count(identity)
      )

  def fetchUserProgress(user: User): F[OverallUserProgress] = {
    def getLibraryProgress(library: Library): F[OverallUserProgressItem] =
      getCompletedSectionCount(user, library).map { completedSections =>
        OverallUserProgressItem(
          libraryName = library.name,
          completedSections = completedSections,
          totalSections = library.sections.size
        )
      }

    for {
      allLibraries    <- EO.getLibraries
      libraryProgress <- allLibraries.traverse(getLibraryProgress)
    } yield OverallUserProgress(libraries = libraryProgress)
  }

  def fetchMaybeUserProgressByLibrary(user: Option[User], libraryName: String): F[LibraryProgress] =
    user.fold(anonymousUserProgressByLibrary(libraryName))(
      fetchUserProgressByLibrary(_, libraryName)
    )

  private[this] def anonymousUserProgressByLibrary(libraryName: String): F[LibraryProgress] =
    for {
      lib <- EO.getLibrary(libraryName)
      sections = lib.foldMap(
        _.sections.map(s =>
          SectionProgress(
            sectionName = s.name,
            succeeded = false
          )
        )
      )
    } yield LibraryProgress(libraryName, sections)

  def fetchUserProgressByLibrary(user: User, libraryName: String): F[LibraryProgress] = {
    def getSectionProgress(section: Section): F[SectionProgress] =
      UO.isSectionCompleted(user, libraryName, section).map { completed =>
        SectionProgress(
          sectionName = section.name,
          succeeded = completed
        )
      }

    for {
      maybeLib <- EO.getLibrary(libraryName)
      libSections = maybeLib.foldMap(_.sections)
      sectionProgress <- libSections.traverse(getSectionProgress)
    } yield LibraryProgress(
      libraryName = libraryName,
      sections = sectionProgress
    )
  }

  def fetchUserProgressByLibrarySection(
      user: User,
      libraryName: String,
      sectionName: String
  ): F[SectionExercises] =
    for {
      maybeSection <- EO.getSection(libraryName, sectionName)
      evaluations  <- UO.getExerciseEvaluations(user, libraryName, sectionName)
      sectionExercises = maybeSection.foldMap(_.exercises)
      exercises = sectionExercises.map { ex =>
        val maybeEvaluation = evaluations.find(_.method == ex.method)
        ExerciseProgress(
          methodName = ex.method,
          args = maybeEvaluation.foldMap(_.args),
          succeeded = maybeEvaluation.fold(false)(_.succeeded)
        )
      }
      totalExercises = sectionExercises.size
    } yield SectionExercises(libraryName, sectionName, exercises, totalExercises)

}

object UserExercisesProgress {
  implicit def instance[F[_]: Monad](implicit
      UO: UserProgressOps[F],
      EO: ExerciseOps[F]
  ): UserExercisesProgress[F] = new UserExercisesProgress[F]

}
