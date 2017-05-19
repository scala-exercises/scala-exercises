/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
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

import org.scalaexercises.types.user.{User, UserCreation}
import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import cats.free._
import freestyle._
import freestyle.implicits._

/** Exposes User operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
@free trait UserOps {
  def getUsers: FS[List[User]]
  def getUserByLogin(login: String): FS[Option[User]]
  def createUser(user: UserCreation.Request): FS[UserCreation.Response]
  def updateUser(user: User): FS[Boolean]
  def deleteUser(user: User): FS[Boolean]
  def getOrCreate(user: UserCreation.Request): FS.Seq[UserCreation.Response] =
    getUserByLogin(user.login) flatMap {
      case None => createUser(user)
      case Some(user) => (Either.right(user): UserCreation.Response).pure[FS.Seq]
  }
}
