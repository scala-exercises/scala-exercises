/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.controllers

import cats.data.Xor
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.free.UserOps
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters
import doobie.imports._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import shared.User
import com.fortysevendeg.exercises.services.interpreters.FreeExtensions._

import scala.concurrent.Future
import scalaz.concurrent._

trait AuthenticationModule { self: ProdInterpreters ⇒

  case class UserRequest[A](val userId: String, request: Request[A]) extends WrappedRequest[A](request)

  object AuthenticationAction extends ActionBuilder[UserRequest] {

    override def invokeBlock[A](
      request: Request[A],
      block:   (UserRequest[A]) ⇒ Future[Result]
    ): Future[Result] =
      request.session.get("user") match {
        case Some(userId) ⇒ block(UserRequest(userId, request))
        case None         ⇒ Future.successful(Forbidden)
      }
  }

  def AuthenticatedUser(block: User ⇒ Result)(implicit userOps: UserOps[ExercisesApp], transactor: Transactor[Task]) =
    AuthenticationAction { request ⇒
      userOps.getUserByLogin(request.userId).runTask match {
        case Xor.Right(Some(user)) ⇒ block(user)
        case _                     ⇒ BadRequest("User login not found")
      }
    }

  def AuthenticatedUser[T](bodyParser: BodyParser[JsValue])(block: (T, User) ⇒ Result)(implicit userOps: UserOps[ExercisesApp], transactor: Transactor[Task], format: Reads[T]) =
    AuthenticationAction(bodyParser) { request ⇒
      request.body.validate[T] match {
        case JsSuccess(validatedBody, _) ⇒
          userOps.getUserByLogin(request.userId).runTask match {
            case Xor.Right(Some(user)) ⇒ block(validatedBody, user)
            case _                     ⇒ BadRequest("User login not found")
          }
        case JsError(errors) ⇒
          BadRequest(JsError.toJson(errors))
      }
    }
}
