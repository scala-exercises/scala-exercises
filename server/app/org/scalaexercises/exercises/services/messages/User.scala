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

package org.scalaexercises.exercises.services.messages

import org.scalaexercises.types.user.User

case class GetUserOrCreateRequest(
    login: String,
    name: String,
    githubId: String,
    pictureUrl: String,
    githubUrl: String,
    email: String
)

case class GetUserOrCreateResponse(user: User)

case class GetUserByLoginRequest(login: String)

case class GetUserByLoginResponse(user: Option[User] = None)

case class CreateUserRequest(
    login: String,
    name: String,
    githubId: String,
    pictureUrl: String,
    githubUrl: String,
    email: String
)

case class CreateUserResponse(user: User)
