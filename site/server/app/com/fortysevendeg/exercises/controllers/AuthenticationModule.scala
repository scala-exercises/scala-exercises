/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.controllers

import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

trait AuthenticationModule {

  case class UserRequest[A](val userId: String, request: Request[A]) extends WrappedRequest[A](request)

  object AuthenticationAction extends ActionBuilder[UserRequest] {

    override def invokeBlock[A](
      request: Request[A],
      block:   (UserRequest[A]) ⇒ Future[Result]
    ): Future[Result] = {
      request.session.get("user") match {
        case Some(userId) ⇒ block(UserRequest(userId, request))
        case None         ⇒ Future.successful(Unauthorized)
      }
    }
  }
}

object AuthenticationModule extends AuthenticationModule
