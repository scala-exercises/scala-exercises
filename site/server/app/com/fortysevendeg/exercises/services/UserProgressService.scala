package com.fortysevendeg.exercises.services

import doobie.imports._
import shared._

class UserProgressService {

  def fetchUserProgress(user: User): ConnectionIO[OverallUserProgress] = ???

  def fetchUserProgressByLibrary(user: User, libraryName: String): ConnectionIO[LibrarySections] = ???

  def fetchUserProgressByLibrarySection(user: User, libraryName: String, sectionName: String): ConnectionIO[LibrarySectionArgs] = ???
}

object UserProgressService extends UserProgressService {
  def apply = new UserProgressService()
}