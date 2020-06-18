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

package org.scalaexercises.types.progress

import org.scalaexercises.types.user._
import org.scalaexercises.types.exercises._

case class UserProgress(
    id: Long,
    userId: Long,
    libraryName: String,
    sectionName: String,
    method: String,
    version: Int,
    exerciseType: ExerciseType = Other,
    args: List[String],
    succeeded: Boolean
)

object SaveUserProgress {
  case class Request(
      user: User,
      libraryName: String,
      sectionName: String,
      method: String,
      version: Int,
      exerciseType: ExerciseType,
      args: List[String],
      succeeded: Boolean
  ) {

    def asUserProgress(id: Long): UserProgress =
      UserProgress(
        id,
        user.id,
        libraryName,
        sectionName,
        method,
        version,
        exerciseType,
        args,
        succeeded
      )
  }
}

// Overall progress

case class OverallUserProgressItem(
    libraryName: String,
    completedSections: Int,
    totalSections: Int
) {
  def completed: Boolean = completedSections == totalSections
}

case class OverallUserProgress(libraries: List[OverallUserProgressItem])

// Per-exercise progress

case class ExerciseProgress(methodName: String, args: List[String], succeeded: Boolean)

case class SectionExercises(
    libraryName: String,
    sectionName: String,
    exercises: List[ExerciseProgress],
    totalExercises: Int
) {
  def sectionSucceeded = exercises.filter(_.succeeded).size == totalExercises
}

// Per-section and library progress

case class SectionProgress(
    sectionName: String,
    succeeded: Boolean
)

case class LibraryProgress(
    libraryName: String,
    sections: List[SectionProgress]
) {
  def totalSections: Int     = sections.size
  def completedSections: Int = sections.filter(_.succeeded).size
}
