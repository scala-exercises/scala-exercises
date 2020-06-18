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

package org.scalaexercises.types.user

case class User(
    id: Long,
    login: String,
    name: Option[String],
    githubId: String,
    pictureUrl: String,
    githubUrl: String,
    email: Option[String]
)

object UserCreation {
  abstract class CreationError extends Product with Serializable
  case object DuplicateName    extends CreationError

  case class Request(
      login: String,
      name: Option[String],
      githubId: String,
      pictureUrl: String,
      githubUrl: String,
      email: Option[String]
  ) {

    def asUser(id: Long): User =
      User(id, login, name, githubId, pictureUrl, githubUrl, email)
  }

  type Response = Either[CreationError, User]
}
