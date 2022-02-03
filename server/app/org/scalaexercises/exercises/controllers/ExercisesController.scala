/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
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
 */

package org.scalaexercises.exercises.controllers

import cats.effect.{ConcurrentEffect, IO}
import cats.implicits._
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.algebra.progress.UserProgressOps
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.evaluator.EvaluatorClient
import org.scalaexercises.exercises.utils.ConfigUtils
import org.scalaexercises.evaluator.types._
import org.scalaexercises.types.exercises.ExerciseEvaluation
import org.scalaexercises.types.exercises.ExerciseEvaluation.EvaluationRequest
import org.scalaexercises.types.progress.SaveUserProgress
import org.scalaexercises.types.evaluator.CoreDependency
import org.scalaexercises.types.user.User
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.{Configuration, Logger, Mode}

class ExercisesController(config: Configuration, components: ControllerComponents)(implicit
    exerciseOps: ExerciseOps[IO],
    userOps: UserOps[IO],
    userProgressOps: UserProgressOps[IO],
    ce: ConcurrentEffect[IO],
    configUtils: ConfigUtils,
    BPAnyContent: BodyParser[AnyContent],
    mode: Mode
) extends BaseController
    with JsonFormats
    with AuthenticationModule {

  private lazy val logger = Logger(this.getClass)

  private val evaluatorClient =
    EvaluatorClient[IO](configUtils.evaluatorUrl, configUtils.evaluatorAuthKey)

  def evaluate(libraryName: String, sectionName: String): Action[JsValue] =
    AuthenticatedUserF[ExerciseEvaluation](parse.json) {
      (evaluation: ExerciseEvaluation, user: User) =>
        def eval(
            runtimeInfo: Either[Throwable, EvaluationRequest]
        ): IO[Either[String, EvalResponse]] =
          runtimeInfo match {
            case Left(ex) =>
              logError("eval", "Error while building the evaluation request", Some(ex))
              IO(Either.left(ex.getMessage))
            case Right(evalRequest) =>
              evalRequest match {
                case Left(msg) =>
                  logError("eval", "Error before performing the evaluation")
                  IO(Either.left(msg))
                case Right((resolvers, dependencies, code)) =>
                  evalRemote(resolvers, dependencies, code)
              }
          }

        def evalRemote(
            resolvers: List[String],
            dependencies: List[CoreDependency],
            code: String
        ): IO[Either[String, EvalResponse]] = {
          val evalResponse: IO[EvalResponse] =
            evaluatorClient.evaluates(EvalRequest(resolvers, dependencies.toEvaluatorDeps, code))

          evalResponse.attempt map {
            case Left(evalException) =>
              logError("evalRemote", "Error while evaluating", Some(evalException))
              Either.left(s"Evaluation failed : $evalException")
            case Right(result) =>
              result match {
                case EvalResponse(EvalResponse.messages.ok, _, _, _, _) =>
                  Either.right(result)
                case EvalResponse(msg, value, valueType, consoleOutput, compilationInfos) =>
                  Either.left(formatEvaluationResponse(msg, value, valueType, compilationInfos))
              }
          }
        }

        def mkHttpStatusCodeResponse(evaluationResult: Either[String, EvalResponse]): IO[Result] = {
          IO(evaluationResult match {
            case Left(msg) =>
              BadRequest(s"Evaluation failed : $msg")
            case Right(evalResponse) =>
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
            case Some(e) => logger.error(msg, e)
            case None    => logger.error(msg)
          }
        }

        (for {
          runtimeInfo      <- exerciseOps.buildRuntimeInfo(evaluation).attempt
          evaluationResult <- eval(runtimeInfo)
          httpResponse     <- mkHttpStatusCodeResponse(evaluationResult)
          _ <- userProgressOps.saveUserProgress(mkSaveProgressRequest(evaluationResult.isRight))
        } yield httpResponse).unsafeToFuture()

    }

  override protected def controllerComponents: ControllerComponents = components
}
