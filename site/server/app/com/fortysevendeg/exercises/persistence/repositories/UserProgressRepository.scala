package com.fortysevendeg.exercises.persistence.repositories

import com.fortysevendeg.exercises.persistence.PersistenceImpl
import com.fortysevendeg.exercises.persistence.domain._
import doobie.imports.ConnectionIO
import shared.UserProgress

import scalaz.Scalaz._

trait UserProgressRepository {

  def findByUserId(userId: Long): ConnectionIO[Option[UserProgress]]

  def create(request: CreateUserProgressRequest): ConnectionIO[UserProgress]

  def delete(id: Long): ConnectionIO[Boolean]

  def update(userProgress: UserProgress): ConnectionIO[UserProgress]
}

class UserProgressDoobieRepository(implicit persistence: PersistenceImpl) extends UserProgressRepository {

  def findByUserId(userId: Long): ConnectionIO[Option[UserProgress]] =
    persistence.fetchOption[Long, UserProgress](UserProgressQueries.findByUserId, userId)

  def create(request: CreateUserProgressRequest): ConnectionIO[UserProgress] = {
    val CreateUserProgressRequest(userId, libraryName, sectionName, method, args, succeeded) = request

    findByUserId(userId) flatMap {
      case _@ None ⇒
        persistence
          .updateWithGeneratedKeys[(Long, String, String, String, Option[String], Option[Boolean]), UserProgress](
            UserProgressQueries.insert,
            UserProgressQueries.allFields,
            (userId, libraryName, sectionName, method, args, succeeded)
          )
      case Some(userP) ⇒
        userP.point[ConnectionIO]
    }
  }

  def delete(id: Long): ConnectionIO[Boolean] =
    persistence.update(UserProgressQueries.deleteById) map (_ > 0)

  def update(userProgress: UserProgress): ConnectionIO[UserProgress] = {
    val UserProgress(id, userId, libraryName, sectionName, method, args, succeeded) = userProgress

    persistence
      .updateWithGeneratedKeys[(Long, String, String, String, Option[String], Option[Boolean]), UserProgress](
        UserProgressQueries.update,
        UserProgressQueries.allFields,
        (userId, libraryName, sectionName, method, args, succeeded)
      )
  }
}

object UserProgressDoobieRepository {

  implicit def userProgressRepository(implicit persistence: PersistenceImpl) = new UserProgressDoobieRepository()
}
