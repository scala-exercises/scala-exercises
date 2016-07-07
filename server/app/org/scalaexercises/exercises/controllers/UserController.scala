/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import cats.data.Xor

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

import scalaz.concurrent.Task

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalaexercises.exercises.services.interpreters.FreeExtensions._

class UserController(
    implicit
    userOps: UserOps[ExercisesApp],
    T:       Transactor[Task]
) extends Controller with ProdInterpreters {

  implicit val jsonReader = (__ \ 'github).read[String](minLength[String](2))

  def byLogin(login: String) = Secure(Action.async { implicit request ⇒
    userOps.getUserByLogin(login).runFuture map {
      case Xor.Right(user) ⇒ user match {
        case Some(u) ⇒ Ok(write(u))
        case None    ⇒ NotFound("The user doesn't exist")
      }
      case Xor.Left(error) ⇒ {
        Logger.error(s"Error rendering user $login", error)
        InternalServerError(error.getMessage)
      }
    }
  })
}
