package com.fortysevendeg.exercises.controllers

import cats.data.Xor
import shared.ExerciseEvaluation
import play.api.libs.json.{ JsError, JsSuccess, Json }
import play.api.mvc.{ Action, BodyParsers, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.{ -\/, \/, \/- }

import com.fortysevendeg.exercises.services._

object ExercisesController extends Controller with JsonFormats {

  def evaluate(libraryName: String, sectionName: String) = Action(BodyParsers.parse.json) { request ⇒
    request.body.validate[ExerciseEvaluation] match {
      case JsSuccess(evaluation, _) ⇒
        ExercisesService.evaluate(evaluation) match {
          case Xor.Right(result) ⇒ Ok("Evaluation succeded : " + result)
          case Xor.Left(error)   ⇒ BadRequest("Evaluation failed : " + error)
        }
      case JsError(errors) ⇒
        BadRequest(JsError.toJson(errors))
    }
  }

}
