/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package ui

import org.scalajs.dom.raw.{ HTMLElement }

import cats.data.OptionT
import cats.syntax.option._
import cats.std.list._
import cats.syntax.traverse._

import shared.IO
import IO._
import actions._
import model._
import model.Exercises._
import utils.DomHandler._

object UI {
  val UNSOLVED = "unsolved"
  val FILLED = "filled"
  val ERRORED = "errored"
  val EVALUATING = "evaluating"
  val SOLVED = "solved"

  def noop: IO[Unit] = io {}

  def update(s: State, a: Action): IO[Unit] = a match {
    case Start()                      ⇒ insertInputs
    case SetState(state)              ⇒ reflectState(state)
    case UpdateExercise(method, args) ⇒ toggleExerciseClass(s, method)
    case CompileExercise(method)      ⇒ startCompilation(s, method)
    case CompilationOk(method)        ⇒ setAsSolved(s, method)
    case CompilationFail(method, msg) ⇒ setAsErrored(s, method, msg)
    case _                            ⇒ noop
  }

  def exerciseToClassname(e: ClientExercise): String = {
    e.state match {
      case Unsolved if (e.isFilled) ⇒ FILLED
      case Unsolved                 ⇒ UNSOLVED
      case Evaluating               ⇒ EVALUATING
      case Errored                  ⇒ ERRORED
      case Solved                   ⇒ SOLVED
    }
  }

  def insertInputs: IO[Unit] =
    inputReplacements flatMap replaceInputs

  def reflectState(s: State): IO[Unit] = {
    s.map(reflectExercise).sequence.map(_ ⇒ ())
  }

  def reflectExercise(e: ClientExercise): IO[Unit] = (for {
    exercise ← OptionT(io(findExerciseByMethod(e.method)))
    _ ← OptionT(setClass(exercise, exerciseToClassname(e)) map (_.some))
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
    Exercises.findByMethod(s, method) match {
      case Some(exercise) ⇒ {
        setClassToExercise(method, exerciseToClassname(exercise))
      }
      case _ ⇒ noop
    }
  }

  def setClassToExercise(method: String, style: String): IO[Unit] = (for {
    exercise ← OptionT(io(findExerciseByMethod(method)))
    _ ← OptionT(setClass(exercise, style) map (_.some))
  } yield ()).value.map(_.getOrElse(()))

  def addLogToExercise(method: String, msg: String): IO[Unit] = (for {
    exercise ← OptionT(io(findExerciseByMethod(method)))
    _ ← OptionT(writeLog(exercise, msg) map (_.some))
  } yield ()).value.map(_.getOrElse(()))

  def startCompilation(s: State, method: String): IO[Unit] = findByMethod(s, method) match {
    case Some(exercise) if exercise.isFilled ⇒ setClassToExercise(method, EVALUATING)
    case _                                   ⇒ noop
  }

  def setAsSolved(s: State, method: String): IO[Unit] = for {
    _ ← setClassToExercise(method, SOLVED)
  } yield ()

  def setAsErrored(s: State, method: String, msg: String): IO[Unit] = for {
    _ ← addLogToExercise(method, msg)
    _ ← setClassToExercise(method, ERRORED)
  } yield ()
}
