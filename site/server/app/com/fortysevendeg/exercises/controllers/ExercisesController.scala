package com.fortysevendeg.exercises.controllers

import cats.data.Xor
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.shared.free.ExerciseOps
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._
import com.fortysevendeg.shared.free.ExerciseOps

import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress
import com.fortysevendeg.exercises.services.free.{ UserOps, UserProgressOps }
import doobie.imports._
import play.api.libs.json.{ JsError, JsSuccess, JsValue }
import play.api.mvc.{ Action, BodyParsers, Controller, Request }
import shared.ExerciseEvaluation

import scalaz.concurrent.Task

class ExercisesController(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[ExercisesApp],
    T:               Transactor[Task]
) extends Controller with JsonFormats {

  def evaluate(libraryName: String, sectionName: String) = Action(BodyParsers.parse.json) { request ⇒
    request.body.validate[ExerciseEvaluation] match {
      case JsSuccess(evaluation, _) ⇒
        exerciseOps.evaluate(evaluation).runTask match {
          case Xor.Right(result) ⇒ Ok("Evaluation succeded : " + result)
          case Xor.Left(error)   ⇒ BadRequest("Evaluation failed : " + error)
        }
      case JsError(errors) ⇒
        BadRequest(JsError.toJson(errors))
    }
  }

  private[this] def updateUserProgress(
    request:    Request[JsValue],
    evaluation: ExerciseEvaluation,
    success:    Boolean
  ) =
    userOps
      .getUserByLogin(request.session.get("user").getOrElse(""))
      .map {
        case Some(user) ⇒
          userProgressOps.saveUserProgress(
            SaveUserProgress.Request(
              userId = user.id,
              libraryName = evaluation.libraryName,
              sectionName = evaluation.sectionName,
              method = evaluation.method,
              args = evaluation.args.headOption map (_ ⇒ evaluation.args.mkString("[", ",", "]")),
              succeeded = success
            )
          )
      }
}
