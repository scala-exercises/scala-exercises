/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package ui

import org.scalajs.dom.raw.{ HTMLElement }

import cats.data.OptionT
import cats.syntax.option._
import cats.std.list._
import cats.syntax.traverse._

import fp._
import IO._
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
  def noop: IO[Unit] = io {}

  def update(s: State, a: Action): IO[Unit] = a match {
    case Start                        ⇒ insertInputs
    case SetState(state)              ⇒ reflectState(state)
    case UpdateExercise(method, args) ⇒ toggleExerciseClass(s, method)
    case CompileExercise(method)      ⇒ startCompilation(s, method)
    case CompilationOk(method)        ⇒ setAsSolved(s, method)
    case CompilationFail(method, msg) ⇒ setAsErrored(s, method, msg)
    case _                            ⇒ noop
  }

  def exerciseStyle(e: ClientExercise): ExerciseStyle = {
    e.state match {
      case Unsolved if (e.isFilled) ⇒ FilledStyle
      case Unsolved                 ⇒ UnsolvedStyle
      case Evaluating               ⇒ EvaluatingStyle
      case Errored                  ⇒ ErroredStyle
      case Solved                   ⇒ SolvedStyle
    }
  }

  def insertInputs: IO[Unit] =
    inputReplacements flatMap replaceInputs

  def reflectState(s: State): IO[Unit] = {
    s.map(reflectExercise).sequence.map(_ ⇒ ())
  }

  def reflectExercise(e: ClientExercise): IO[Unit] = (for {
    exercise ← OptionT(io(findExerciseByMethod(e.method)))
    _ ← OptionT(setExerciseStyle(e.method, exerciseStyle(e), if (e.isSolved) "solved-exercise" else "") map (_.some))
    _ ← OptionT(copyArgumentsToInputs(e, exercise) map (_.some))
  } yield ()).value.map(_.getOrElse(()))

  def copyArgumentsToInputs(e: ClientExercise, element: HTMLElement): IO[Unit] = {
    val theInputs = inputs(element)
    val args = e.arguments
    args.zip(theInputs).map({ argAndInput ⇒
      val (arg, input) = argAndInput
      setInputValue(input, arg)
    }).toList.sequence.map(_ ⇒ ())
  }

  def toggleExerciseClass(s: State, method: String): IO[Unit] = {
    Exercises.findByMethod(s, method).fold(noop)(exercise ⇒
      setExerciseStyle(method, exerciseStyle(exercise), if (exercise.isSolved) "solved-exercise" else ""))
  }

  def setExerciseStyle(method: String, style: ExerciseStyle, codeStyle: String = ""): IO[Unit] = (for {
    exercise ← OptionT(io(findExerciseByMethod(method)))
    _ ← OptionT(setExerciseClass(exercise, style.className) map (_.some))
    code ← OptionT(io(findExerciseCode(exercise)))
    _ ← OptionT(setCodeClass(code, codeStyle) map (_.some))
  } yield ()).value.map(_.getOrElse(()))

  def addLogToExercise(method: String, msg: String): IO[Unit] = (for {
    exercise ← OptionT(io(findExerciseByMethod(method)))
    _ ← OptionT(writeLog(exercise, msg) map (_.some))
  } yield ()).value.map(_.getOrElse(()))

  def startCompilation(s: State, method: String): IO[Unit] = (isLogged, findByMethod(s, method)) match {
    case (true, Some(exercise)) if exercise.isFilled ⇒ setExerciseStyle(method, EvaluatingStyle)
    case (false, _) ⇒ showSignUpModal
    case _ ⇒ noop
  }

  def setAsSolved(s: State, method: String): IO[Unit] =
    setExerciseStyle(method, SolvedStyle, "solved-exercise")

  def setAsErrored(s: State, method: String, msg: String): IO[Unit] = for {
    _ ← addLogToExercise(method, msg)
    _ ← setExerciseStyle(method, ErroredStyle)
  } yield ()
}
