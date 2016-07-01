/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaExercises.exercises.controllers

import cats.data.Xor
import org.scalaExercises.exercises.Secure
import org.scalaExercises.exercises.app._
import org.scalaExercises.exercises.services.free.UserOps
import org.scalaExercises.exercises.services.interpreters.ProdInterpreters
import doobie.imports._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import upickle._

import scalaz.concurrent.Task

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalaExercises.exercises.services.interpreters.FreeExtensions._

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
      case Xor.Left(error) ⇒ InternalServerError(error.getMessage)
    }
  })
}
