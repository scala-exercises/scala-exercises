/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.repositories

import com.fortysevendeg.exercises.persistence.PersistenceModule
import com.fortysevendeg.exercises.persistence.domain._
import doobie.imports.ConnectionIO
import shared.UserProgress

import scalaz.Scalaz._

trait UserProgressRepository {

  def findByUserId(userId: Long): ConnectionIO[Option[UserProgress]]

  def findByExerciseVersion(
    userId:      Long,
    libraryName: String,
    sectionName: String,
    method:      String,
    version:     Int
  ): ConnectionIO[Option[UserProgress]]

  def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress]

  def delete(id: Long): ConnectionIO[Boolean]

  def update(userProgress: UserProgress): ConnectionIO[UserProgress]
}

class UserProgressDoobieRepository(implicit persistence: PersistenceModule) extends UserProgressRepository {

  override def findByUserId(userId: Long): ConnectionIO[Option[UserProgress]] =
    persistence.fetchOption[Long, UserProgress](UserProgressQueries.findByUserId, userId)

  override def findByExerciseVersion(
    userId:      Long,
    libraryName: String,
    sectionName: String,
    method:      String,
    version:     Int
  ): ConnectionIO[Option[UserProgress]] = persistence.fetchOption[(Long, String, String, String, Int), UserProgress](
    UserProgressQueries.findByExerciseVersion, (userId, libraryName, sectionName, method, version)
  )

  override def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress] = {
    val SaveUserProgress.Request(userId, libraryName, sectionName, method, version, exerciseType, args, succeeded) = request

    findByExerciseVersion(userId, libraryName, sectionName, method, version) flatMap {
      case None ⇒
        persistence
          .updateWithGeneratedKeys[(Long, String, String, String, Int, String, Option[String], Boolean), UserProgress](
            UserProgressQueries.insert,
            "id" :: UserProgressQueries.allFields,
            (userId, libraryName, sectionName, method, version, exerciseType.toString, args, succeeded)
          )
      case Some(userP) ⇒
        userP.point[ConnectionIO]
    }
  }

  override def delete(id: Long): ConnectionIO[Boolean] =
    persistence.update(UserProgressQueries.deleteById) map (_ > 0)

  override def update(userProgress: UserProgress): ConnectionIO[UserProgress] = {
    val UserProgress(_, userId, libraryName, sectionName, method, version, exerciseType, args, succeeded) = userProgress

    persistence
      .updateWithGeneratedKeys[(Long, String, String, String, Int, String, Option[String], Boolean), UserProgress](
        UserProgressQueries.update,
        UserProgressQueries.allFields,
        (userId, libraryName, sectionName, method, version, exerciseType, args, succeeded)
      )
  }
}

object UserProgressDoobieRepository {

  implicit def instance(implicit persistence: PersistenceModule) = new UserProgressDoobieRepository()
}
