package org.scalaexercises.algebra.exercises

import org.scalaexercises.types.exercises._
import cats.free._
import org.scalaexercises.types.exercises.ExerciseEvaluation.EvaluationRequest
import io.freestyle._

/** Exposes Exercise operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
@free trait ExerciseOps[F[_]] {

  def getLibraries: Free[F, List[Library]]

  def getLibrary(libraryName: String): Free[F, Option[Library]] =
    getLibraries map (_.find(_.name == libraryName))

  def getSection(libraryName: String, sectionName: String): Free[F, Option[Section]]

  def buildRuntimeInfo(evaluation: ExerciseEvaluation): Free[F, EvaluationRequest]

}
