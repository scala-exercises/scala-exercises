/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.exercises.Secure
import cats.data.Xor

import org.scalaexercises.algebra.app._
import org.scalaexercises.types.user.User
import org.scalaexercises.algebra.user.UserOps

import org.scalaexercises.exercises.services.interpreters.ProdInterpreters

import doobie.imports._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._

import org.scalaexercises.exercises.services.interpreters.FreeExtensions._

import scala.concurrent.ExecutionContext.Implicits.global

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

  def AuthenticatedUser(block: User ⇒ Future[Result])(implicit userOps: UserOps[ExercisesApp], transactor: Transactor[Task]) =
    Secure(AuthenticationAction.async { request ⇒
      userOps.getUserByLogin(request.userId).runFuture flatMap {
        case Xor.Right(Some(user)) ⇒ block(user)
        case _                     ⇒ Future.successful(BadRequest("User login not found"))
      }
    })

  def AuthenticatedUser[T](bodyParser: BodyParser[JsValue])(block: (T, User) ⇒ Future[Result])(implicit userOps: UserOps[ExercisesApp], transactor: Transactor[Task], format: Reads[T]) =
    Secure(AuthenticationAction.async(bodyParser) { request ⇒
      request.body.validate[T] match {
        case JsSuccess(validatedBody, _) ⇒
          userOps.getUserByLogin(request.userId).runFuture flatMap {
            case Xor.Right(Some(user)) ⇒ block(validatedBody, user)
            case _                     ⇒ Future.successful(BadRequest("User login not found"))
          }
        case JsError(errors) ⇒
          Future.successful(BadRequest(JsError.toJson(errors)))
      }
    })
}
