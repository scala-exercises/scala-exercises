/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalaexercises.exercises.persistence.repositories

import org.scalaexercises.types.user._
import org.scalaexercises.exercises.persistence.PersistenceModule
import org.scalaexercises.exercises.persistence.domain.{UserQueries => Q}
import org.scalaexercises.exercises.persistence.repositories.UserRepository._

import doobie._
import cats.implicits._

trait UserRepository {

  def all: ConnectionIO[List[User]]

  def getByLogin(login: String): ConnectionIO[Option[User]]

  def getById(id: Long): ConnectionIO[Option[User]]

  def create(user: UserCreation.Request): ConnectionIO[UserCreation.Response]

  def deleteAll(): ConnectionIO[Int]

  def delete(id: Long): ConnectionIO[Boolean]

  def update(user: User): ConnectionIO[Option[User]]
}

class UserDoobieRepository(implicit persistence: PersistenceModule) extends UserRepository {

  override def all: ConnectionIO[List[User]] =
    persistence.fetchList[User](Q.all)

  override def getByLogin(login: String): ConnectionIO[Option[User]] =
    persistence.fetchOption[String, User](Q.findByLogin, login)

  override def getById(id: Long): ConnectionIO[Option[User]] =
    persistence.fetchOption[Long, User](Q.findById, id)

  override def create(request: UserCreation.Request): ConnectionIO[UserCreation.Response] = {
    val UserCreation.Request(login, name, githubId, pictureUrl, githubUrl, email) = request

    for {
      _ <-
        persistence
          .updateWithGeneratedKeys[InsertParams, User](
            Q.insert,
            Q.allFields,
            (login, name, githubId, pictureUrl, githubUrl, email)
          )
      user <- getByLogin(login)
    } yield Either.fromOption(user, UserCreation.DuplicateName)
  }

  override def delete(id: Long): ConnectionIO[Boolean] =
    persistence.update[Long](Q.deleteById, id) map (_ > 0)

  def deleteAll(): ConnectionIO[Int] = persistence.update(Q.deleteAll)

  def update(user: User): ConnectionIO[Option[User]] =
    for {
      _ <-
        persistence
          .updateWithGeneratedKeys[UpdateParams, User](
            Q.update,
            Q.allFields,
            (user.name, user.githubId, user.pictureUrl, user.githubUrl, user.email, user.id)
          )
      maybeUser <- getById(user.id)
    } yield maybeUser
}

object UserRepository {

  // Queries input:
  type UpdateParams = (Option[String], String, String, String, Option[String], Long)
  type InsertParams = (String, Option[String], String, String, String, Option[String])

  implicit def instance(implicit persistence: PersistenceModule): UserRepository =
    new UserDoobieRepository()
}
