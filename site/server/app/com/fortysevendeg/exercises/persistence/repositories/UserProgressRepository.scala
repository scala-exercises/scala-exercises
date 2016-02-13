package com.fortysevendeg.exercises.persistence.repositories

import com.fortysevendeg.exercises.persistence.PersistenceModule
import com.fortysevendeg.exercises.persistence.domain._
import doobie.imports.ConnectionIO
import shared.UserProgress
import com.fortysevendeg.exercises.persistence.domain.{ UserProgressQueries ⇒ Q }

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

  def update(userProgress: UserProgress, newSucceededValue: Boolean): ConnectionIO[UserProgress]
}

class UserProgressDoobieRepository(implicit persistence: PersistenceModule) extends UserProgressRepository {

  override def findByUserId(userId: Long): ConnectionIO[Option[UserProgress]] =
    persistence.fetchOption[Long, UserProgress](Q.findByUserId, userId)

  override def findByExerciseVersion(
    userId:      Long,
    libraryName: String,
    sectionName: String,
    method:      String,
    version:     Int
  ): ConnectionIO[Option[UserProgress]] = persistence.fetchOption[(Long, String, String, String, Int), UserProgress](
    Q.findByExerciseVersion, (userId, libraryName, sectionName, method, version)
  )

  override def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress] = {
    val SaveUserProgress.Request(userId, libraryName, sectionName, method, version, exerciseType, args, succeeded) = request

    findByExerciseVersion(userId, libraryName, sectionName, method, version) flatMap {
      case None ⇒
        persistence
          .updateWithGeneratedKeys[(Long, String, String, String, Int, String, Option[String], Boolean), UserProgress](
            Q.insert,
            Q.allFields,
            (userId, libraryName, sectionName, method, version, exerciseType.toString, args, succeeded)
          )
      case Some(userP) ⇒
        update(userP, succeeded)
    }
  }

  override def delete(id: Long): ConnectionIO[Boolean] =
    persistence.update(Q.deleteById) map (_ > 0)

  override def update(userProgress: UserProgress, newSucceededValue: Boolean): ConnectionIO[UserProgress] = {
    val UserProgress(id, _, libraryName, sectionName, method, version, exerciseType, args, _) = userProgress

    persistence
      .updateWithGeneratedKeys[(String, String, String, Int, String, Option[String], Boolean, Long), UserProgress](
        Q.update,
        Q.allFields,
        (libraryName, sectionName, method, version, exerciseType, args, newSucceededValue, id)
      )
  }
}

object UserProgressDoobieRepository {

  implicit def instance(implicit persistence: PersistenceModule) = new UserProgressDoobieRepository()
}
