package controllers

import models.UserModel
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import services.parser.ExercisesService
import upickle._

import scala.concurrent.Future

object UserController extends Controller {

  implicit val jsonReader = (__ \ 'github).read[String](minLength[String](2))

  def index = Action { implicit request =>
    Redirect("/")
  }

  def all = Action.async { implicit request =>
    UserModel.store.all.map { r =>
      Ok(write(r))
    }.recover { case err =>
      InternalServerError(err.getMessage)
    }
  }

}
