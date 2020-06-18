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
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.exercises.Secure
import org.scalaexercises.types.user.User
import play.api.Mode
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationModule {
  case class UserRequest[A](userId: String, request: Request[A]) extends WrappedRequest[A](request)

  class AuthenticationAction(implicit bodyParser: BodyParser[AnyContent])
      extends ActionBuilder[UserRequest, AnyContent] {

    override def invokeBlock[A](
        request: Request[A],
        thunk: (UserRequest[A]) => Future[Result]
    ): Future[Result] = {
      request.session.get("user") match {
        case Some(userId) => thunk(UserRequest(userId, request))
        case None         => Future.successful(Forbidden)
      }
    }

    override def parser: BodyParser[AnyContent] = bodyParser

    override protected def executionContext: ExecutionContext = global
  }

  def AuthenticatedUser(
      thunk: User => IO[Result]
  )(implicit userOps: UserOps[IO], mode: Mode, bodyParser: BodyParser[AnyContent]) =
    Secure(new AuthenticationAction().async { request =>
      (userOps.getUserByLogin(request.userId) flatMap {
        case Some(user) => thunk(user)
        case _          => IO.pure(BadRequest("User login not found"))
      }).unsafeToFuture()
    })

  def AuthenticatedUserF[T](parser: BodyParser[JsValue])(thunk: (T, User) => Future[Result])(
      implicit
      userOps: UserOps[IO],
      mode: Mode,
      bodyParser: BodyParser[AnyContent],
      format: Reads[T]
  ) =
    Secure(new AuthenticationAction().async(parser) { request =>
      request.body.validate[T] match {
        case JsSuccess(validatedBody, _) =>
          userOps.getUserByLogin(request.userId).unsafeToFuture() flatMap {
            case Some(user) => thunk(validatedBody, user)
            case _          => Future.successful(BadRequest("User login not found"))
          }
        case JsError(errors) =>
          Future.successful(BadRequest(JsError.toJson(errors)))
      }
    })
}
