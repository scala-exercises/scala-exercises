package controllers

import models.ExerciseEvaluation
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, BodyParsers, Controller}
import services.parser.ExercisesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.{-\/, \/, \/-}

object ExercisesController extends Controller with JsonFormats {

  def sections = Action.async { implicit request =>
    Future(Ok(Json.toJson(ExercisesService.sections)))
  }

  def category(section : String, category : String) = Action.async { implicit request =>
    Future(Ok(Json.toJson(ExercisesService.category(section, category))))
  }

  def evaluate(section : String, category : String) = Action(BodyParsers.parse.json) { request =>
    request.body.validate[ExerciseEvaluation] match {
      case JsSuccess(evaluation, _) =>
        ExercisesService.evaluate(evaluation) match {
          case \/-(result) => Ok("Evaluation succeded : " + result)
          case -\/(error) => BadRequest("Evaluation failed : " + error)
        }
      case JsError(errors) =>
        BadRequest(JsError.toJson(errors))
    }
  }

}
