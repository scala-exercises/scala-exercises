package org.scalaexercises.algebra.exercises

import org.scalaexercises.types.exercises._
import org.scalaexercises.types.exercises.ExerciseEvaluation.EvaluationRequest

import freestyle._

/** Exposes Exercise operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
@free trait ExerciseOps[F[_]] {
  def getLibraries: FreeS[F, List[Library]]

  def getLibrary(libraryName: String): FreeS[F, Option[Library]] =
    getLibraries map (_.find(_.name == libraryName))

  def getSection(libraryName: String, sectionName: String): FreeS[F, Option[Section]]

  def buildRuntimeInfo(evaluation: ExerciseEvaluation): FreeS[F, EvaluationRequest]

}
