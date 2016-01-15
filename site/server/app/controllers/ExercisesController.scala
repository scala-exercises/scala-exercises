package controllers

import shared.ExerciseEvaluation
import play.api.libs.json.{ JsError, JsSuccess, Json }
import play.api.mvc.{ Action, BodyParsers, Controller }
import services.ExercisesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.{ -\/, \/, \/- }

object ExercisesController extends Controller with JsonFormats {

  def libraries = Action.async { implicit request ⇒
    Future(Ok(Json.toJson(ExercisesService.libraries)))
  }

  def section(libraryName: String, sectionName: String) = Action.async { implicit request ⇒
    Future(Ok(Json.toJson(ExercisesService.section(libraryName, sectionName))))
  }

  def evaluate(libraryName: String, sectionName: String) = Action(BodyParsers.parse.json) { request ⇒
    request.body.validate[ExerciseEvaluation] match {
      case JsSuccess(evaluation, _) ⇒
        ExercisesService.evaluate(evaluation) match {
          case \/-(result) ⇒ Ok("Evaluation succeded : " + result)
          case -\/(error)  ⇒ BadRequest("Evaluation failed : " + error)
        }
      case JsError(errors) ⇒
        BadRequest(JsError.toJson(errors))
    }
  }

}
