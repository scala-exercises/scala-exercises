package org.scalaexercises.types.progress

import cats.{ Foldable, NonEmptyReducible, Reducible }
import cats.data.{ NonEmptyList, OneAnd }
import cats.std.list._
import org.scalaexercises.types.user._
import org.scalaexercises.types.exercises._

case class UserProgress(
  id:           Long,
  userId:       Long,
  libraryName:  String,
  sectionName:  String,
  method:       String,
  version:      Int,
  exerciseType: ExerciseType = Other,
  args:         List[String],
  succeeded:    Boolean
)

object SaveUserProgress {
  case class Request(
      user:         User,
      libraryName:  String,
      sectionName:  String,
      method:       String,
      version:      Int,
      exerciseType: ExerciseType,
      args:         List[String],
      succeeded:    Boolean
  ) {

    def asUserProgress(id: Long): UserProgress =
      UserProgress(id, user.id, libraryName, sectionName, method, version, exerciseType, args, succeeded)
  }
}

// Overall progress

case class OverallUserProgressItem(
    libraryName:       String,
    completedSections: Int,
    totalSections:     Int
) {
  def completed: Boolean = completedSections == totalSections
}

case class OverallUserProgress(libraries: List[OverallUserProgressItem])

// Per-exercise progress

case class ExerciseProgress(methodName: String, args: List[String], succeeded: Boolean)

case class SectionExercises(
    libraryName:    String,
    sectionName:    String,
    exercises:      List[ExerciseProgress],
    totalExercises: Int
) {
  def sectionSucceeded = exercises.count(_.succeeded) == totalExercises
}

// Per-section and library progress

case class SectionProgress(
  sectionName: String,
  succeeded:   Boolean
)

case class LibraryProgress(
    libraryName: String,
    sections:    NonEmptyList[SectionProgress]
) {
  def totalSections: Int = sections.tail.size + 1
  def completedSections: Int = sections.filter(_.succeeded).size
}
