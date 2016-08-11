package org.scalaexercises.algebra

import cats.free._
import org.scalaexercises.types.evaluator._
import org.scalaexercises.types.exercises.ExerciseEvaluation

sealed trait EvaluatorOp[A]
final case class Evaluates(
  url:          String,
  authKey:      String,
  resolvers:    List[String]     = Nil,
  dependencies: List[Dependency] = Nil,
  code:         String
)
    extends EvaluatorOp[ExerciseEvaluation.Result]

class EvaluatorOps[F[_]](implicit I: Inject[EvaluatorOp, F]) {

  def evaluates(
    url:          String,
    authKey:      String,
    resolvers:    List[String]     = Nil,
    dependencies: List[Dependency] = Nil,
    code:         String
  ): Free[F, ExerciseEvaluation.Result] =
    Free.inject[EvaluatorOp, F](
      Evaluates(url, authKey, resolvers, dependencies, code)
    )

}

object EvaluatorOps {

  implicit def instance[F[_]](
    implicit
    I: Inject[EvaluatorOp, F]
  ): EvaluatorOps[F] = new EvaluatorOps[F]

}
