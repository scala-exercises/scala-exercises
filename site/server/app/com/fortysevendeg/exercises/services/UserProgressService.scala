package com.fortysevendeg.exercises.services

import com.fortysevendeg.exercises.persistence.repositories.UserProgressDoobieRepository
import com.fortysevendeg.exercises.persistence.repositories.UserProgressDoobieRepository.{ instance ⇒ repository }
import doobie.imports._
import shared._

class UserProgressService {

  def fetchUserProgress(user: User): ConnectionIO[OverallUserProgress] = ???

  def fetchUserProgressByLibrary(user: User, libraryName: String): ConnectionIO[LibrarySections] = ???

  def fetchUserProgressByLibrarySection(
    user:        User,
    libraryName: String,
    sectionName: String
  ): ConnectionIO[LibrarySectionArgs] = {
    repository.findBySection(user.id, libraryName, sectionName) map {
      list ⇒
        val exercisesList: List[LibrarySectionExercise] = list map { up ⇒
          val argsList: List[String] = up.args map (_.split(",").toList) getOrElse List.empty

          LibrarySectionExercise(up.method, argsList, up.succeeded)
        }
        val succeeded = exercisesList.forall(_.succeeded)
        LibrarySectionArgs(libraryName, 0, exercisesList, succeeded)
    }
  }
}

object UserProgressService extends UserProgressService {
  def apply = new UserProgressService()
}