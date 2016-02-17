/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.repositories

import com.fortysevendeg.exercises.persistence.PersistenceModule
import com.fortysevendeg.exercises.persistence.domain._
import doobie.imports._
import shared.{ LibrarySectionExercise, User, UserProgress }
import com.fortysevendeg.exercises.persistence.domain.{ UserProgressQueries ⇒ Q }

case class SectionProgress(libraryName: String, succeeded: Boolean, exerciseList: List[LibrarySectionExercise])

trait UserProgressRepository {

  def findById(id: Long): ConnectionIO[Option[UserProgress]]

  def findByUserId(userId: Long): ConnectionIO[List[UserProgress]]

  def findByExerciseVersion(
    userId:      Long,
    libraryName: String,
    sectionName: String,
    method:      String,
    version:     Int
  ): ConnectionIO[Option[UserProgress]]

  def findBySection(userId: Long, libraryName: String, sectionName: String): ConnectionIO[List[UserProgress]]

  def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress]

  def delete(id: Long): ConnectionIO[Boolean]

  def deleteAll(): ConnectionIO[Int]

  def update(userProgress: UserProgress, newSucceededValue: Boolean): ConnectionIO[UserProgress]

  def findUserProgress(
    user:        User,
    libraryName: String,
    sectionName: String
  ): ConnectionIO[SectionProgress] =
    findBySection(user.id, libraryName, sectionName) map {
      list ⇒
        val exercisesList: List[LibrarySectionExercise] = list map { up ⇒
          val argsList: List[String] = up.args map (_.split("##").toList) getOrElse Nil
          LibrarySectionExercise(up.method, argsList, up.succeeded)
        }
        val succeeded = exercisesList.forall(_.succeeded)
        SectionProgress(libraryName, succeeded, exercisesList)
    }

}

class UserProgressDoobieRepository(implicit persistence: PersistenceModule) extends UserProgressRepository {

  override def findById(id: Long): ConnectionIO[Option[UserProgress]] =
    persistence.fetchOption[Long, UserProgress](Q.findById, id)

  override def findByUserId(userId: Long): ConnectionIO[List[UserProgress]] =
    persistence.fetchList[Long, UserProgress](Q.findByUserId, userId)

  override def findByExerciseVersion(
    userId:      Long,
    libraryName: String,
    sectionName: String,
    method:      String,
    version:     Int
  ): ConnectionIO[Option[UserProgress]] = persistence.fetchOption[(Long, String, String, String, Int), UserProgress](
    Q.findByExerciseVersion, (userId, libraryName, sectionName, method, version)
  )

  override def findBySection(
    userId:      Long,
    libraryName: String,
    sectionName: String
  ): ConnectionIO[List[UserProgress]] = persistence.fetchList[(Long, String, String), UserProgress](
    Q.findBySection, (userId, libraryName, sectionName)
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

  override def delete(id: Long): ConnectionIO[Boolean] = persistence.update[Long](Q.deleteById, id) map (_ > 0)

  override def deleteAll(): ConnectionIO[Int] = persistence.update(Q.deleteAll)

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

object UserProgressRepository {

  implicit def instance(implicit persistence: PersistenceModule): UserProgressRepository = new UserProgressDoobieRepository

}
