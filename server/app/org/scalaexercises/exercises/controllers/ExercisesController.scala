/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import cats.data.Xor
import org.scalaexercises.exercises.app._
import org.scalaexercises.exercises.persistence.domain.SaveUserProgress
import org.scalaexercises.exercises.services.free.{ UserOps, UserProgressOps }
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import org.scalaexercises.shared.free.ExerciseOps
import doobie.imports._
import play.api.libs.json.JsValue
import play.api.mvc.{ Action, BodyParsers, Controller }
import shared.{ ExerciseEvaluation, User }

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalaexercises.exercises.services.interpreters.FreeExtensions._

import scalaz.concurrent.Task

class ExercisesController(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[ExercisesApp],
    T:               Transactor[Task]
) extends Controller with JsonFormats with AuthenticationModule with ProdInterpreters {

  def evaluate(libraryName: String, sectionName: String): Action[JsValue] =
    AuthenticatedUser[ExerciseEvaluation](BodyParsers.parse.json) {
      (evaluation: ExerciseEvaluation, user: User) ⇒
        val eval = for {
          exerciseEvaluation ← exerciseOps.evaluate(evaluation = evaluation)
          _ ← userProgressOps.saveUserProgress(
            mkSaveProgressRequest(user, evaluation, exerciseEvaluation.isRight)
          )
        } yield exerciseEvaluation

        eval.runFuture.map {
          case Xor.Left(e) ⇒ BadRequest(s"Evaluation failed : $e")
          case Xor.Right(r) ⇒ r.fold(
            msg ⇒ BadRequest(msg),
            v ⇒ Ok(s"Evaluation succeeded : $v")
          )
        }
    }

  private[this] def mkSaveProgressRequest(user: User, evaluation: ExerciseEvaluation, success: Boolean) =
    new SaveUserProgress.Request(
      user = user,
      libraryName = evaluation.libraryName,
      sectionName = evaluation.sectionName,
      method = evaluation.method,
      version = evaluation.version,
      exerciseType = evaluation.exerciseType,
      args = evaluation.args,
      succeeded = success
    )
}
