package org.scalaexercises.algebra

import cats.free._
import org.scalaexercises.types.evaluator._
import org.scalaexercises.types.exercises.ExerciseEvaluation

import scala.concurrent.duration._
import scala.concurrent.duration.Duration

sealed trait EvaluatorOp[A]
final case class Evaluates(
  url:          String,
  authKey:      String,
  connTimeout:  Duration,
  readTimeout:  Duration,
  resolvers:    List[String]     = Nil,
  dependencies: List[Dependency] = Nil,
  code:         String
)
    extends EvaluatorOp[ExerciseEvaluation.Result]

class EvaluatorOps[F[_]](implicit I: Inject[EvaluatorOp, F]) {

  def evaluates(
    url:          String,
    authKey:      String,
    connTimeout:  Duration         = 1.second,
    readTimeout:  Duration         = 30.seconds,
    resolvers:    List[String]     = Nil,
    dependencies: List[Dependency] = Nil,
    code:         String
  ): Free[F, ExerciseEvaluation.Result] =
    Free.inject[EvaluatorOp, F](
      Evaluates(
        url = url,
        authKey = authKey,
        connTimeout = connTimeout,
        readTimeout = readTimeout,
        resolvers = resolvers,
        dependencies = dependencies,
        code = code
      )
    )

}

object EvaluatorOps {

  implicit def instance[F[_]](
    implicit
    I: Inject[EvaluatorOp, F]
  ): EvaluatorOps[F] = new EvaluatorOps[F]

}
