package com.fortysevendeg.exercises.controllers

import cats.data.Xor
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.controllers.AuthenticationModule._
import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress
import com.fortysevendeg.exercises.services.free.{ UserOps, UserProgressOps }
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._
import com.fortysevendeg.exercises.utils.StringUtils.ExerciseType
import com.fortysevendeg.shared.free.ExerciseOps
import doobie.imports._
import play.api.libs.json.{ JsError, JsSuccess, JsValue }
import play.api.mvc.{ Action, BodyParsers, Controller }
import shared.ExerciseEvaluation

import scalaz.concurrent.Task

class ExercisesController(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[ExercisesApp],
    T:               Transactor[Task]
) extends Controller with JsonFormats {

  def evaluate(libraryName: String, sectionName: String): Action[JsValue] =
    AuthenticationAction(BodyParsers.parse.json) { request ⇒
      request.body.validate[ExerciseEvaluation] match {
        case JsSuccess(evaluation, _) ⇒
          val eval = for {
            exerciseEvaluation ← exerciseOps.evaluate(evaluation = evaluation)
            _ ← userProgressOps.saveUserProgress(
              mkSaveProgressRequest(request.userId, evaluation, exerciseEvaluation.isRight)
            )
          } yield exerciseEvaluation

          eval.runTask match {
            case Xor.Right(result) ⇒ Ok("Evaluation succeded : " + result)
            case Xor.Left(error)   ⇒ BadRequest("Evaluation failed : " + error)
          }
        case JsError(errors) ⇒
          BadRequest(JsError.toJson(errors))
      }
    }

  private[this] def mkSaveProgressRequest(userId: String, evaluation: ExerciseEvaluation, success: Boolean) =
    new SaveUserProgress.Request(
      userId = userId.toLong,
      libraryName = evaluation.libraryName,
      sectionName = evaluation.sectionName,
      method = evaluation.method,
      version = evaluation.version,
      exerciseType = ExerciseType fromString evaluation.exerciseType,
      args = evaluation.args.headOption map (_ ⇒ evaluation.args.mkString("[", ",", "]")),
      succeeded = success
    )
}
