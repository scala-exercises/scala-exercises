/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package model

sealed trait ExerciseState
case object Unsolved extends ExerciseState
case object Evaluating extends ExerciseState
case object Errored extends ExerciseState
case object Solved extends ExerciseState

case class ClientExercise(
    library:   String,
    section:   String,
    method:    String,
    arguments: Seq[String]   = Nil,
    state:     ExerciseState = Unsolved
) {

  def isFilled: Boolean = !arguments.exists(_.trim.isEmpty) && arguments.nonEmpty

  def isSolved: Boolean = state == Solved

  def isBeingEvaluated: Boolean = state == Evaluating

  def canBeCompiled: Boolean = isFilled && !isBeingEvaluated
}

object Exercises {
  type State = List[ClientExercise]

  def findByMethod(s: State, method: String): Option[ClientExercise] =
    s.find(_.method == method)

  def applyByMethod(s: State, method: String, f: ClientExercise ⇒ ClientExercise): State =
    s.map(ex ⇒ {
      if (ex.method == method)
        f(ex)
      else
        ex
    })

  def updateByMethod(s: State, method: String, args: Seq[String]): State =
    applyByMethod(s, method, _.copy(arguments = args, state = Unsolved))

  def evaluate(s: State, method: String): State = findByMethod(s, method) match {
    case Some(exercise) if exercise.canBeCompiled ⇒ applyByMethod(s, method, _.copy(state = Evaluating))
    case _                                        ⇒ s
  }

  def setAsSolved(s: State, method: String): State =
    applyByMethod(s, method, _.copy(state = Solved))

  def setAsErrored(s: State, method: String): State =
    applyByMethod(s, method, _.copy(state = Errored))
}
