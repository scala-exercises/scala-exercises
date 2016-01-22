package ui

import shared.IO
import IO._
import actions._
import model._
import model.Exercises._
import utils.DomHandler._

object UI {
  def noop: IO[Unit] = io {}

  def update(s: State, a: Action): IO[Unit] = a match {
    case UpdateExercise(method, args) ⇒ toggleExerciseClass(s, method)
    case CompileExercise(method)      ⇒ startCompilation(s, method)
    case CompilationOk(method)        ⇒ setAsSolved(s, method)
    case CompilationFail(method)      ⇒ setAsErrored(s, method)
    case _                            ⇒ noop
  }

  def toggleExerciseClass(s: State, method: String): IO[Unit] = {
    Exercises.findByMethod(s, method) match {
      case Some(exercise) ⇒ {
        if (exercise.isFilled)
          setClassToExercise(method, "filled")
        else
          setClassToExercise(method, "unsolved")
      }
      case _ ⇒ noop
    }
  }

  def setClassToExercise(method: String, style: String): IO[Unit] = findExerciseByMethod(method)
    .map(_.foreach(setClass(_, style).unsafePerformIO()))

  def addLogToExercise(method: String, msg: String): IO[Unit] = findExerciseByMethod(method).map(_.foreach(writeLog(_, msg).unsafePerformIO()))

  def startCompilation(s: State, method: String): IO[Unit] = findByMethod(s, method) match {
    case Some(exercise) if exercise.isFilled ⇒ setClassToExercise(method, "evaluating")
    case _                                   ⇒ noop
  }

  def setAsSolved(s: State, method: String): IO[Unit] = for {
    _ ← setClassToExercise(method, "solved")
    _ = println(s"EXERCISE SOLVED $method")
  } yield ()

  def setAsErrored(s: State, method: String): IO[Unit] = for {
    _ ← addLogToExercise(method, "Failed compilation")
    _ ← setClassToExercise(method, "errored")
    _ = println(s"EXERCISE ERRORED $method")
  } yield ()
}
