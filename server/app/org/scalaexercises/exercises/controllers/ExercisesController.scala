/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import cats.data.Xor
import cats.free.Free
import cats.syntax.xor._
import org.scalaexercises.types.user.User
import org.scalaexercises.types.exercises.ExerciseEvaluation
import org.scalaexercises.types.progress.SaveUserProgress
import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.algebra.progress.UserProgressOps
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import doobie.imports._
import org.scalaexercises.algebra.EvaluatorOps
import org.scalaexercises.evaluator.EvalResponse
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.{ Action, BodyParsers, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalaexercises.exercises.services.interpreters.FreeExtensions._
import org.scalaexercises.exercises.utils.ConfigUtils
import org.scalaexercises.types.evaluator.Dependency
import org.scalaexercises.types.exercises.ExerciseEvaluation.Result

import scalaz.concurrent.Task

class ExercisesController(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[ExercisesApp],
    evaluatorOps:    EvaluatorOps[ExercisesApp],
    T:               Transactor[Task]
) extends Controller with JsonFormats with AuthenticationModule with ProdInterpreters {

  def evaluate(libraryName: String, sectionName: String): Action[JsValue] =
    AuthenticatedUser[ExerciseEvaluation](BodyParsers.parse.json) {
      (evaluation: ExerciseEvaluation, user: User) ⇒

        val eval = for {
          buildRuntimeInfo ← exerciseOps.buildRuntimeInfo(evaluation = evaluation)
          evaluationResult ← buildRuntimeInfo match {
            case Xor.Left(msg) ⇒ Free.pure[ExercisesApp, Result](msg.left)
            case Xor.Right((resolvers, dependencies, code)) ⇒
              evaluateAndSaveProgress(evaluation, user, resolvers, dependencies, code)
          }
        } yield evaluationResult

        eval.runFuture.map {
          case Xor.Left(e) ⇒
            Logger.error("Error while evaluating $evaluation with user $user", e)
            BadRequest(s"Evaluation failed : $e")
          case Xor.Right(r) ⇒ r.fold(
            msg ⇒ BadRequest(msg),
            v ⇒ Ok(s"Evaluation succeeded : $v")
          )
        }
    }

  private[this] def evaluateAndSaveProgress(
    evaluation:   ExerciseEvaluation,
    user:         User,
    resolvers:    List[String],
    dependencies: List[Dependency],
    code:         String
  ): Free[ExercisesApp, Result] = for {
    evaluationResult ← evaluatorOps.evaluates(
      url = ConfigUtils.evaluatorUrl,
      authKey = ConfigUtils.evaluatorAuthKey,
      readTimeout = ConfigUtils.evaluatorReadTimeout,
      resolvers = resolvers,
      dependencies = dependencies,
      code = code
    )
    analyzedResult ← evaluationResult match {
      case Xor.Left(msg) ⇒
        Free.pure[ExercisesApp, Result](msg.left)
      case Xor.Right(EvalResponse(EvalResponse.messages.ok, _, _, _)) ⇒
        Free.pure[ExercisesApp, Result](evaluationResult)
      case Xor.Right(EvalResponse(msg, value, valueType, compilationInfos)) ⇒
        Free.pure[ExercisesApp, Result](
          formatEvaluationResponse(msg, value, valueType, compilationInfos).left
        )
    }
    _ ← userProgressOps.saveUserProgress(
      mkSaveProgressRequest(user, evaluation, analyzedResult.isRight)
    )
  } yield analyzedResult

  private[this] def mkSaveProgressRequest(user: User, evaluation: ExerciseEvaluation, success: Boolean) =
    SaveUserProgress.Request(
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
