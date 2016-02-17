package com.fortysevendeg.exercises.services

import cats.free.Free
import com.fortysevendeg.exercises.persistence.repositories.UserProgressDoobieRepository.{ instance ⇒ repository }
import com.fortysevendeg.exercises.services.free.ConnectionIOOps._
import com.fortysevendeg.shared.free.ExerciseOps
import doobie.imports._
import shared._

import scalaz.concurrent.Task

class UserProgressService[F[_]](implicit exerciseOps: ExerciseOps[F], transactor: Transactor[Task]) {

  def fetchUserProgress(user: User): Free[F, OverallUserProgress] = ???

  def fetchUserProgressByLibrary(user: User, libraryName: String): Free[F, LibrarySections] = ???

  def fetchUserProgressByLibrarySection(
    user:        User,
    libraryName: String,
    sectionName: String
  ): Free[F, LibrarySectionArgs] =
    for {
      lbs ← findUserProgress(user, libraryName, sectionName).liftF[F]
      sectionCount ← exerciseOps.getLibrary(libraryName) map (_ map (_.sections.size) getOrElse 0)
    } yield LibrarySectionArgs(libraryName, sectionCount, lbs.exerciseList, lbs.succeeded)

  private[this] case class SectionProgress(libraryName: String, succeeded: Boolean, exerciseList: List[LibrarySectionExercise])

  private[this] def findUserProgress(
    user:        User,
    libraryName: String,
    sectionName: String
  ): ConnectionIO[SectionProgress] =
    repository.findBySection(user.id, libraryName, sectionName) map {
      list ⇒
        val exercisesList: List[LibrarySectionExercise] = list map { up ⇒
          val argsList: List[String] = up.args map (_.split("##").toList) getOrElse Nil

          LibrarySectionExercise(up.method, argsList, up.succeeded)
        }
        val succeeded = exercisesList.forall(_.succeeded)
        SectionProgress(libraryName, succeeded, exercisesList)
    }
}

object UserProgressService extends UserProgressService {
  def instance[F[_]](implicit exerciseOps: ExerciseOps[F]) = new UserProgressService[F]
}