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

package org.scalaexercises.exercises.controllers

import org.scalaexercises.exercises.Secure

import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.user.UserOps

import org.scalaexercises.exercises.services.interpreters.ProdInterpreters

import doobie.imports._

import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import upickle._
import upickle.default._

import scalaz.concurrent.Task

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalaexercises.exercises.services.interpreters.FreeExtensions._

import freestyle._
import freestyle.implicits._

class UserController(
    implicit userOps: UserOps[ExercisesApp.Op],
    T: Transactor[Task]
) extends Controller
    with ProdInterpreters {

  implicit val jsonReader = (__ \ 'github).read[String](minLength[String](2))

  def byLogin(login: String) =
    Secure(Action.async { implicit request ⇒
      userOps.getUserByLogin(login).runFuture map {
        case Right(user) ⇒
          user match {
            case Some(u) ⇒ Ok(write(u))
            case None    ⇒ NotFound("The user doesn't exist")
          }
        case Left(error) ⇒ {
          Logger.error(s"Error rendering user $login", error)
          InternalServerError(error.getMessage)
        }
      }
    })
}
