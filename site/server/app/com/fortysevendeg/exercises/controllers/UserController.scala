package com.fortysevendeg.exercises.controllers

import doobie.imports._
import scalaz.concurrent.Task
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import upickle._
import cats.data.Xor

import com.fortysevendeg.exercises.services.free.UserOps
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._

import scala.concurrent.Future

class UserController(
    implicit
    userOps: UserOps[ExercisesApp],
    T:       Transactor[Task]
) extends Controller {

  implicit val jsonReader = (__ \ 'github).read[String](minLength[String](2))

  def all = Action { implicit request ⇒
    userOps.getUsers runTask match {
      case Xor.Right(users) ⇒ Ok(write(users))
      case Xor.Left(error)  ⇒ InternalServerError(error.getMessage)
    }
  }

  def byLogin(login: String) = Action { implicit request ⇒
    userOps.getUserByLogin(login) runTask match {
      case Xor.Right(user) ⇒ user match {
        case Some(u) ⇒ Ok(write(u))
        case None       ⇒ NotFound("The user doesn't exist")
      }
      case Xor.Left(error) ⇒ InternalServerError(error.getMessage)
    }
  }
}
