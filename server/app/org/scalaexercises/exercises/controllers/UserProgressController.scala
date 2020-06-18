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

package org.scalaexercises.exercises.controllers

import cats.effect.IO
import org.scalaexercises.algebra.progress.UserExercisesProgress
import org.scalaexercises.algebra.user.UserOps
import play.api.Mode
import play.api.libs.json.Json
import play.api.mvc._

class UserProgressController(components: ControllerComponents)(implicit
    userOps: UserOps[IO],
    exercisesProgress: UserExercisesProgress[IO],
    bodyParser: BodyParser[AnyContent],
    mode: Mode
) extends BaseController
    with JsonFormats
    with AuthenticationModule {

  def fetchUserProgressBySection(libraryName: String, sectionName: String) =
    AuthenticatedUser { user =>
      exercisesProgress.fetchUserProgressByLibrarySection(user, libraryName, sectionName) map {
        response => Ok(Json.toJson(response))
      }
    }

  override protected def controllerComponents: ControllerComponents = components
}
