package com.fortysevendeg.exercises.controllers

import com.fortysevendeg.exercises.models.UserStore
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import upickle._
import com.fortysevendeg.exercises.services._
import com.fortysevendeg.exercises.services.free.UserOps
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._

import scala.concurrent.Future
import scalaz.{ -\/, \/, \/- }

class UserController(implicit userOps: UserOps[ExercisesApp], userServices: UserServices) extends Controller {

  implicit val jsonReader = (__ \ 'github).read[String](minLength[String](2))

  def index = Action { implicit request ⇒
    Redirect("/")
  }

  def all = Action { implicit request ⇒
    userOps.getUsers runTask match {
      case \/-(users) ⇒ Ok(write(users))
      case -\/(error) ⇒ InternalServerError(error.getMessage)
    }
  }

  def byLogin(login: String) = Action { implicit request ⇒
    userOps.getUserByLogin(login) runTask match {
      case \/-(user) ⇒ user match {
        case Some(user) ⇒ Ok(write(user))
        case None       ⇒ NotFound("The user doesn't exist")
      }
      case -\/(error) ⇒ InternalServerError(error.getMessage)
    }
  }
}
