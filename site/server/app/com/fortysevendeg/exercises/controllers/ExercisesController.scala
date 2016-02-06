package com.fortysevendeg.exercises.controllers

import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress
import com.fortysevendeg.exercises.services.free.{ UserProgressOps, UserOps }
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._
import com.fortysevendeg.shared.free.ExerciseOps
import doobie.imports._
import play.api.libs.json.{ JsValue, JsError, JsSuccess }
import play.api.mvc.{ Request, Action, BodyParsers, Controller }
import shared.ExerciseEvaluation

import scalaz.concurrent.Task
import scalaz.{ -\/, \/- }

class ExercisesController(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[UserAndUserProgressOps],
    T:               Transactor[Task]
) extends Controller with JsonFormats {

  def evaluate(libraryName: String, sectionName: String) = Action(BodyParsers.parse.json) { request ⇒
    request.body.validate[ExerciseEvaluation] match {
      case JsSuccess(evaluation, _) ⇒

        val eval = for {
          exerciseEvaluation ← exerciseOps.evaluate(evaluation = evaluation)
          _ = updateUserProgress(request, evaluation, exerciseEvaluation.isRight)
        } yield exerciseEvaluation

        eval.runTask match {
          case \/-(result) ⇒ Ok("Evaluation succeded : " + result)
          case -\/(error)  ⇒ BadRequest("Evaluation failed : " + error)
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
