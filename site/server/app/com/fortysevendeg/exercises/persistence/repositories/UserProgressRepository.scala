/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.repositories

import com.fortysevendeg.exercises.persistence.PersistenceModule
import com.fortysevendeg.exercises.persistence.domain._
import com.fortysevendeg.exercises.persistence.repositories.UserProgressRepository._
import doobie.imports._
import shared.{ SectionInfoItem, LibrarySectionExercise, User, UserProgress }
import com.fortysevendeg.exercises.persistence.domain.{ UserProgressQueries ⇒ Q }
import doobie.contrib.postgresql.pgtypes._

case class SectionProgress(libraryName: String, succeeded: Boolean, exerciseList: List[LibrarySectionExercise])

trait UserProgressRepository {

  def findById(id: Long): ConnectionIO[Option[UserProgress]]

  def findByUserId(userId: Long): ConnectionIO[List[UserProgress]]

  def findByUserIdAggregated(userId: Long): ConnectionIO[List[FindByUserIdAggregatedOutput]]

  def findByLibrary(
    userId:      Long,
    libraryName: String
  ): ConnectionIO[List[FindByLibraryOutput]]

  def completedSectionsByLibrary(
    userId:      Long,
    libraryName: String
  ): ConnectionIO[Int] = for {
    lib ← findByLibrary(userId, libraryName)
  } yield lib.filter(_._2).size

  def findBySection(userId: Long, libraryName: String, sectionName: String): ConnectionIO[List[UserProgress]]

  def findByExerciseVersion(
    userId:      Long,
    libraryName: String,
    sectionName: String,
    method:      String,
    version:     Int
  ): ConnectionIO[Option[UserProgress]]

  def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress]

  def delete(id: Long): ConnectionIO[Boolean]

  def deleteAll(): ConnectionIO[Int]

  def update(userProgress: UserProgress): ConnectionIO[UserProgress]

  def findUserProgressBySection(
    user:        User,
    libraryName: String,
    sectionName: String
  ): ConnectionIO[SectionProgress]

  def findUserProgressByLibrary(
    user:        User,
    libraryName: String
  ): ConnectionIO[List[SectionInfoItem]]
}

class UserProgressDoobieRepository(implicit persistence: PersistenceModule) extends UserProgressRepository {

  override def findById(id: Long): ConnectionIO[Option[UserProgress]] =
    persistence.fetchOption[Long, UserProgress](Q.findById, id)

  override def findByUserId(userId: Long): ConnectionIO[List[UserProgress]] =
    persistence.fetchList[Long, UserProgress](Q.findByUserId, userId)

  override def findByLibrary(
    userId:      Long,
    libraryName: String
  ): ConnectionIO[List[FindByLibraryOutput]] =
    persistence.fetchList[FindByLibraryParams, FindByLibraryOutput](
      Q.findByLibrary, (userId, libraryName)
    )

  override def findByUserIdAggregated(userId: Long): ConnectionIO[List[FindByUserIdAggregatedOutput]] =
    persistence.fetchList[Long, FindByUserIdAggregatedOutput](Q.findByUserIdAggregated, userId)

  override def findBySection(
    userId:      Long,
    libraryName: String,
    sectionName: String
  ): ConnectionIO[List[UserProgress]] = persistence.fetchList[FindBySectionParams, UserProgress](
    Q.findBySection, (userId, libraryName, sectionName)
  )

  override def findByExerciseVersion(
    userId:      Long,
    libraryName: String,
    sectionName: String,
    method:      String,
    version:     Int
  ): ConnectionIO[Option[UserProgress]] = persistence.fetchOption[FindByExerciseVerionParams, UserProgress](
    Q.findByExerciseVersion, (userId, libraryName, sectionName, method, version)
  )

  override def findUserProgressByLibrary(
    user:        User,
    libraryName: String
  ): ConnectionIO[List[SectionInfoItem]] =
    findByLibrary(user.id, libraryName) map {
      list ⇒
        list map { up ⇒
          SectionInfoItem(sectionName = up._1, succeeded = up._2)
        }
    }

  override def findUserProgressBySection(
    user:        User,
    libraryName: String,
    sectionName: String
  ): ConnectionIO[SectionProgress] =
    findBySection(user.id, libraryName, sectionName) map {
      list ⇒
        val exercisesList: List[LibrarySectionExercise] = list map { up ⇒
          LibrarySectionExercise(up.method, up.args, up.succeeded)
        }
        val succeeded = exercisesList match {
          case Nil ⇒ false
          case _   ⇒ exercisesList.forall(_.succeeded)
        }
        SectionProgress(libraryName, succeeded, exercisesList)
    }

  override def create(request: SaveUserProgress.Request): ConnectionIO[UserProgress] = {
    val SaveUserProgress.Request(userId, libraryName, sectionName, method, version, exerciseType, args, succeeded) = request

    findByExerciseVersion(userId, libraryName, sectionName, method, version) flatMap {
      case None ⇒
        persistence
          .updateWithGeneratedKeys[InsertParams, UserProgress](
            Q.insert,
            Q.allFields,
            (userId, libraryName, sectionName, method, version, exerciseType.toString, args, succeeded)
          )
      case Some(userP) ⇒
        update(request.asUserProgress(userP.id))
    }
  }

  override def delete(id: Long): ConnectionIO[Boolean] = persistence.update[Long](Q.deleteById, id) map (_ > 0)

  override def deleteAll(): ConnectionIO[Int] = persistence.update(Q.deleteAll)

  override def update(userProgress: UserProgress): ConnectionIO[UserProgress] = {
    val UserProgress(id, _, libraryName, sectionName, method, version, exerciseType, args, succeeded) = userProgress

    persistence
      .updateWithGeneratedKeys[UpdateParams, UserProgress](
        Q.update,
        Q.allFields,
        (libraryName, sectionName, method, version, exerciseType, args, succeeded, id)
      )
  }

}

object UserProgressRepository {

  //Queries input:
  type FindByLibraryParams = (Long, String)
  type FindBySectionParams = (Long, String, String)
  type FindByExerciseVerionParams = (Long, String, String, String, Int)
  type UpdateParams = (String, String, String, Int, String, List[String], Boolean, Long)
  type InsertParams = (Long, String, String, String, Int, String, List[String], Boolean)

  //Queries output:
  type FindByLibraryOutput = (String, Boolean)
  type FindByUserIdAggregatedOutput = (String, Long, Boolean)

  implicit def instance(implicit persistence: PersistenceModule): UserProgressRepository = new UserProgressDoobieRepository
}
