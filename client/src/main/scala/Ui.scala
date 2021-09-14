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

package org.scalaexercises.client
package ui

import org.scalajs.dom.raw.HTMLElement

import cats.data.OptionT
import cats.implicits._

import monix.eval.Coeval

import actions._
import model._
import model.Exercises._
import utils.DomHandler._

sealed trait ExerciseStyle {
  def className: String
}
case object UnsolvedStyle extends ExerciseStyle {
  val className = "unsolved"
}
case object FilledStyle extends ExerciseStyle {
  val className = "filled"
}
case object ErroredStyle extends ExerciseStyle {
  val className = "errored"
}
case object EvaluatingStyle extends ExerciseStyle {
  val className = "evaluating"
}
case object SolvedStyle extends ExerciseStyle {
  val className = "solved"
}

object UI {
  def noop: Coeval[Unit] = Coeval {}

  def update(s: State, a: Action): Coeval[Unit] =
    a match {
      case Start                        => noop
      case SetState(state)              => reflectState(state)
      case UpdateExercise(method, args) => toggleExerciseClass(s, method)
      case CompileExercise(method)      => startCompilation(s, method)
      case CompilationOk(method)        => setAsSolved(s, method)
      case CompilationFail(method, msg) => setAsErrored(s, method, msg)
      case _                            => noop
    }

  def exerciseStyle(e: ClientExercise): ExerciseStyle = {
    e.state match {
      case Unsolved if (e.isFilled) => FilledStyle
      case Unsolved                 => UnsolvedStyle
      case Evaluating               => EvaluatingStyle
      case Errored                  => ErroredStyle
      case Solved                   => SolvedStyle
    }
  }

  def insertInputs: Coeval[Unit] =
    inputReplacements flatMap replaceInputs

  def reflectState(s: State): Coeval[Unit] =
    s.map(reflectExercise).sequence.map(_ => ())

  def reflectExercise(e: ClientExercise): Coeval[Unit] =
    (for {
      exercise <- OptionT(Coeval(findExerciseByMethod(e.method)))
      _ <- OptionT {
        setExerciseStyle(
          e.method,
          exerciseStyle(e),
          if (e.isSolved) "solved-exercise" else ""
        ) map (_.some)
      }
      _ <- OptionT(copyArgumentsToInputs(e, exercise) map (_.some))
    } yield ()).value.map(_.getOrElse(()))

  def copyArgumentsToInputs(e: ClientExercise, element: HTMLElement): Coeval[Unit] = {
    val theInputs = inputs(element)
    val args      = e.arguments
    args
      .zip(theInputs)
      .map { case (arg, input) => setInputValue(input, arg) }
      .toList
      .sequence
      .map(_ => ())
  }

  def toggleExerciseClass(s: State, method: String): Coeval[Unit] = {
    Exercises
      .findByMethod(s, method)
      .fold(noop)(exercise =>
        setExerciseStyle(
          method,
          exerciseStyle(exercise),
          if (exercise.isSolved) "solved-exercise"
          else ""
        )
      )
  }

  def setExerciseStyle(method: String, style: ExerciseStyle, codeStyle: String = ""): Coeval[Unit] =
    (for {
      exercise <- OptionT(Coeval(findExerciseByMethod(method)))
      _        <- OptionT(setExerciseClass(exercise, style.className) map (_.some))
      code     <- OptionT(Coeval(findExerciseCode(exercise)))
      _        <- OptionT(setCodeClass(code, codeStyle) map (_.some))
    } yield ()).value.map(_.getOrElse(()))

  def addLogToExercise(method: String, msg: String): Coeval[Unit] =
    (for {
      exercise <- OptionT(Coeval(findExerciseByMethod(method)))
      _        <- OptionT(writeLog(exercise, msg) map (_.some))
    } yield ()).value.map(_.getOrElse(()))

  def startCompilation(s: State, method: String): Coeval[Unit] =
    (isLogged, findByMethod(s, method)) match {
      case (true, Some(exercise)) if exercise.isFilled => setExerciseStyle(method, EvaluatingStyle)
      case (false, _)                                  => showSignUpModal
      case _                                           => noop
    }

  def setAsSolved(s: State, method: String): Coeval[Unit] =
    setExerciseStyle(method, SolvedStyle, "solved-exercise")

  def setAsErrored(s: State, method: String, msg: String): Coeval[Unit] =
    for {
      _ <- addLogToExercise(method, msg)
      _ <- setExerciseStyle(method, ErroredStyle)
    } yield ()
}
