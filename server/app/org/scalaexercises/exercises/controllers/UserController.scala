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

package org.scalaexercises.exercises.controllers

import cats.effect.IO
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.exercises.Secure
import org.scalaexercises.types.user.User
import play.api.Mode
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import upickle.default._

class UserController(mode: Mode, components: ControllerComponents)(implicit userOps: UserOps[IO])
    extends BaseController {

  implicit val jsonReader = (__ \ 'github).read[String](minLength[String](2))

  implicit val userWriter: Writer[User] = macroW

  def byLogin(login: String) =
    Secure(mode)(Action.async { _ ⇒
      (userOps.getUserByLogin(login) map {
        case Some(u) ⇒ Ok(write(u))
        case None    ⇒ NotFound("The user doesn't exist")
      }).unsafeToFuture()
    })

  override protected def controllerComponents: ControllerComponents = components
}
