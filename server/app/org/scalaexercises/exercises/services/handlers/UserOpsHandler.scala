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

package org.scalaexercises.exercises.services.handlers

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.exercises.persistence.repositories.UserRepository
import org.scalaexercises.types.user.UserCreation.Response
import org.scalaexercises.types.user.{User, UserCreation}

class UserOpsHandler[F[_]](implicit F: Sync[F], T: Transactor[F]) extends UserOps[F] {

  private val repo = UserRepository.instance

  override def getUsers: F[List[User]] = repo.all.transact(T)

  override def getUserByLogin(login: String): F[Option[User]] = repo.getByLogin(login).transact(T)

  override def createUser(user: UserCreation.Request): F[Response] = repo.create(user).transact(T)

  override def updateUser(user: User): F[Boolean] = repo.update(user).map(_.isDefined).transact(T)

  override def deleteUser(user: User): F[Boolean] = repo.delete(user.id).transact(T)
}
