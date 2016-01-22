package com.fortysevendeg.exercises.controllers

import com.fortysevendeg.exercises.models.UserStore
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import upickle._

import scala.concurrent.Future

class UserController(userStore: UserStore) extends Controller {

  implicit val jsonReader = (__ \ 'github).read[String](minLength[String](2))

  def index = Action { implicit request ⇒
    Redirect("/")
  }

  def all = Action.async { implicit request ⇒
    userStore.all.map { r ⇒
      Ok(write(r))
    }.recover {
      case err ⇒
        InternalServerError(err.getMessage)
    }
  }

}
