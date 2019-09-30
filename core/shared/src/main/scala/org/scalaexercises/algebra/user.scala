/*
 *  scala-exercises
 *
 *  Copyright 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
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
 *
 */

package org.scalaexercises.algebra.user

import cats.Monad
import cats.implicits._
import org.scalaexercises.types.user.UserCreation.Response
import org.scalaexercises.types.user.{User, UserCreation}

/** Exposes User operations as a Free monadic algebra that may be combined with other Algebras via
 * Coproduct
 */
trait UserOpsAlgebra[F[_]] {
  def getUsers: F[List[User]]
  def getUserByLogin(login: String): F[Option[User]]
  def createUser(user: UserCreation.Request): F[UserCreation.Response]
  def updateUser(user: User): F[Boolean]
  def deleteUser(user: User): F[Boolean]
  def getOrCreate(user: UserCreation.Request): F[UserCreation.Response]
}

abstract class UserOps[F[_]: Monad] extends UserOpsAlgebra[F] {
  def getOrCreate(user: UserCreation.Request): F[Response] =
    getUserByLogin(user.login) flatMap {
      case None       => createUser(user)
      case Some(user) => (Either.right(user): UserCreation.Response).pure[F]
    }
}
