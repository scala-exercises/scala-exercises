package com.fortysevendeg.exercises.persistence.repositories

import com.fortysevendeg.exercises.persistence.PersistenceModule
import com.fortysevendeg.exercises.persistence.domain._
import doobie.imports.ConnectionIO
import shared.UserProgress

import scalaz.Scalaz._

trait UserProgressRepository {

  def findByUserId(userId: Long): ConnectionIO[Option[UserProgress]]

  def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress]

  def delete(id: Long): ConnectionIO[Boolean]

  def update(userProgress: UserProgress): ConnectionIO[UserProgress]
}

class UserProgressDoobieRepository(implicit persistence: PersistenceModule) extends UserProgressRepository {

  def findByUserId(userId: Long): ConnectionIO[Option[UserProgress]] =
    persistence.fetchOption[Long, UserProgress](UserProgressQueries.findByUserId, userId)

  def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress] = {
    val SaveUserProgress.Request(userId, libraryName, sectionName, method, version, exerciseType, args, succeeded) = request

    findByUserId(userId) flatMap {
      case None ⇒
        persistence
          .updateWithGeneratedKeys[(Long, String, String, String, Int, String, Option[String], Boolean), UserProgress](
            UserProgressQueries.insert,
            UserProgressQueries.allFields,
            (userId, libraryName, sectionName, method, version, exerciseType.toString, args, succeeded)
          )
      case Some(userP) ⇒
        userP.point[ConnectionIO]
    }
  }

  def delete(id: Long): ConnectionIO[Boolean] =
    persistence.update(UserProgressQueries.deleteById) map (_ > 0)

  def update(userProgress: UserProgress): ConnectionIO[UserProgress] = {
    val UserProgress(id, userId, libraryName, sectionName, method, version, exerciseType, args, succeeded) = userProgress

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
