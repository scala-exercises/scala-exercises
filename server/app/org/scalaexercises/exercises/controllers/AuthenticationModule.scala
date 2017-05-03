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

import cats.MonadError
import org.scalaexercises.exercises.Secure
import org.scalaexercises.algebra.app._
import org.scalaexercises.types.user.User
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import doobie.imports._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.concurrent._
import freestyle._
import freestyle.implicits._
import cats.instances.future._

trait AuthenticationModule { self: ProdInterpreters ⇒
  case class UserRequest[A](val userId: String, request: Request[A])
      extends WrappedRequest[A](request)

  object AuthenticationAction extends ActionBuilder[UserRequest] {

    override def invokeBlock[A](
        request: Request[A],
        thunk: (UserRequest[A]) ⇒ Future[Result]
    ): Future[Result] = {
      request.session.get("user") match {
        case Some(userId) ⇒ thunk(UserRequest(userId, request))
        case None         ⇒ Future.successful(Forbidden)
      }
    }
  }

  def AuthenticatedUser(thunk: User ⇒ FreeS[ExercisesApp.Op, Result])(
      implicit userOps: UserOps[ExercisesApp.Op],
      transactor: Transactor[Task]) =
    Secure(AuthenticationAction.async { request ⇒
      FreeS.liftPar(userOps.getUserByLogin(request.userId)) flatMap {
        case Some(user) ⇒ thunk(user)
        case _          ⇒ FreeS.pure(BadRequest("User login not found"))
      }
    })

  def AuthenticatedUserF[T](bodyParser: BodyParser[JsValue])(thunk: (T, User) ⇒ Future[Result])(
      implicit userOps: UserOps[ExercisesApp.Op],
      transactor: Transactor[Task],
      I: ParInterpreter[ExercisesApp.Op, Task],
      format: Reads[T]) =
    Secure(AuthenticationAction.async(bodyParser) { request ⇒
      request.body.validate[T] match {
        case JsSuccess(validatedBody, _) ⇒
          FreeS.liftPar(userOps.getUserByLogin(request.userId)).runFuture flatMap {
            case Right(Some(user)) ⇒ thunk(validatedBody, user)
            case _                 ⇒ Future.successful(BadRequest("User login not found"))
          }
        case JsError(errors) ⇒
          Future.successful(BadRequest(JsError.toJson(errors)))
      }
    })
}
