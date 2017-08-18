/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.scalaexercises.exercises.controllers

import cats.free.Free
import cats.implicits._
import org.scalaexercises.types.user.User
import org.scalaexercises.types.exercises.ExerciseEvaluation
import org.scalaexercises.types.progress.SaveUserProgress
import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.algebra.progress.UserProgressOps
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import doobie.imports._
import org.scalaexercises.evaluator.EvalResponse
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.{Action, BodyParsers, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalaexercises.types.evaluator.Dependency
import org.scalaexercises.types.exercises.ExerciseEvaluation.EvaluationRequest
import org.scalaexercises.evaluator.EvaluatorClient
import org.scalaexercises.evaluator.EvaluatorClient._
import org.scalaexercises.evaluator.EvaluatorResponses.{EvaluationResponse, EvaluationResult}
import org.scalaexercises.evaluator.free.algebra.EvaluatorOp
import org.scalaexercises.evaluator.implicits._

import scala.concurrent.Future
import scalaz.concurrent.Task

import freestyle._
import freestyle.implicits._
import cats.instances.future._

class ExercisesController(
    implicit exerciseOps: ExerciseOps[ExercisesApp.Op],
    userOps: UserOps[ExercisesApp.Op],
    userProgressOps: UserProgressOps[ExercisesApp.Op],
    T: Transactor[Task]
) extends Controller
    with JsonFormats
    with AuthenticationModule
    with ProdInterpreters {

  implicit val interpreter: ParInterpreter[ExercisesApp.Op, Task] = implicitly

  def evaluate(libraryName: String, sectionName: String): Action[JsValue] =
    AuthenticatedUserF[ExerciseEvaluation](BodyParsers.parse.json) {
      (evaluation: ExerciseEvaluation, user: User) ⇒
        def eval(runtimeInfo: Either[Throwable, EvaluationRequest]): Future[
          Either[String, EvalResponse]] =
          runtimeInfo match {
            case Left(ex) ⇒
              logError("eval", "Error while building the evaluation request", Some(ex))
              Future.successful(Either.left(ex.getMessage))
            case Right(evalRequest) ⇒
              evalRequest match {
                case Left(msg) ⇒
                  logError("eval", "Error before performing the evaluation")
                  Future.successful(Either.left(msg))
                case Right((resolvers, dependencies, code)) ⇒
                  evalRemote(resolvers, dependencies, code)
              }
          }

        def evalRemote(
            resolvers: List[String],
            dependencies: List[Dependency],
            code: String
        ): Future[Either[String, EvalResponse]] = {
          val evalResponse: Free[EvaluatorOp, EvaluationResponse[EvalResponse]] =
            evaluatorClient.api.evaluates(resolvers, dependencies.toEvaluatorDeps, code)

          evalResponse.exec map {
            case Left(evalException) ⇒
              logError("evalRemote", "Error while evaluating", Some(evalException))
              Either.left(s"Evaluation failed : $evalException")
            case Right(EvaluationResult(result, statusCode, _)) ⇒
              result match {
                case EvalResponse(EvalResponse.messages.ok, _, _, _, _) ⇒
                  Either.right(result)
                case EvalResponse(msg, value, valueType, consoleOutput, compilationInfos) ⇒
                  Either.left(formatEvaluationResponse(msg, value, valueType, compilationInfos))
              }
          }
        }

        def mkHttpStatusCodeResponse(
            evaluationResult: Either[String, EvalResponse]): Future[Result] = {
          Future.successful(evaluationResult match {
            case Left(msg) ⇒
              BadRequest(s"Evaluation failed : $msg")
            case Right(evalResponse) ⇒
              Ok(s"Evaluation succeeded : $evalResponse")
          })
        }

        def mkSaveProgressRequest(success: Boolean) =
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

        def logError(method: String, mainMsg: String, ex: Option[Throwable] = None) = {
          val msg = s"[$method] $mainMsg. ExerciseEvaluation: $evaluation with user $user"
          ex match {
            case Some(e) ⇒ Logger.error(msg, e)
            case None    ⇒ Logger.error(msg)
          }
        }

        for {
          runtimeInfo ← FreeS
            .liftPar(exerciseOps.buildRuntimeInfo(evaluation = evaluation))
            .runFuture
          evaluationResult ← eval(runtimeInfo)
          httpResponse     ← mkHttpStatusCodeResponse(evaluationResult)
          _ ← FreeS
            .liftPar(
              userProgressOps
                .saveUserProgress(mkSaveProgressRequest(evaluationResult.isRight)))
            .runFuture
        } yield httpResponse
    }
}
