package ui

import shared.IO
import IO._
import actions._
import model._
import model.Exercises._
import utils.DomHandler._

object UI {

  sealed trait ExerciseStyle { def className: String }
  case object Unsolved extends ExerciseStyle { val className = "unsolved" }
  case object Filled extends ExerciseStyle { val className = "filled" }
  case object Evaluating extends ExerciseStyle { val className = "evaluating" }
  case object Errored extends ExerciseStyle { val className = "errored" }
  case object Solved extends ExerciseStyle { val className = "solved" }

  def noop: IO[Unit] = io {}

  def update(s: State, a: Action): IO[Unit] = a match {
    case UpdateExercise(method, args) ⇒ toggleExerciseClass(s, method)
    case CompileExercise(method)      ⇒ startCompilation(s, method)
    case CompilationOk(method)        ⇒ setAsSolved(s, method)
    case CompilationFail(method, msg) ⇒ setAsErrored(s, method, msg)
    case _                            ⇒ noop
  }

  def toggleExerciseClass(s: State, method: String): IO[Unit] = {
    Exercises.findByMethod(s, method) match {
      case Some(exercise) ⇒ {
        if (exercise.isFilled)
          setClassToExercise(method, Filled.className)
        else
          setClassToExercise(method, Unsolved.className)
      }
      case _ ⇒ noop
    }
  }

  def setClassToExercise(method: String, style: String): IO[Unit] = findExerciseByMethod(method)
    .map(_.foreach(setClass(_, style).unsafePerformIO()))

  def addLogToExercise(method: String, msg: String): IO[Unit] = {
    findExerciseByMethod(method).map(_.foreach(writeLog(_, msg).unsafePerformIO()))
  }

  def startCompilation(s: State, method: String): IO[Unit] = findByMethod(s, method) match {
    case Some(exercise) if exercise.isFilled ⇒ setClassToExercise(method, Evaluating.className)
    case _                                   ⇒ noop
  }

  def setAsSolved(s: State, method: String): IO[Unit] = for {
    _ ← setClassToExercise(method, Solved.className)
  } yield ()

  def setAsErrored(s: State, method: String, msg: String): IO[Unit] = for {
    _ ← addLogToExercise(method, msg)
    _ ← setClassToExercise(method, Errored.className)
  } yield ()
}
