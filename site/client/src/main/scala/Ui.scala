package ui

import shared.IO
import IO._
import actions._
import model._
import model.Exercises._
import utils.{ DomHandler }

object UI {
  def noop: IO[Unit] = io {}

  def update(s: State, a: Action): IO[Unit] = a match {
    case UpdateExercise(method, args) ⇒ toggleCompileButton(s, method)
    case CompileExercise(method)      ⇒ startCompilation(s, method)
    case CompilationOk(method)        ⇒ setAsSolved(s, method)
    case CompilationFail(method)      ⇒ setAsErrored(s, method)
    case _                            ⇒ noop
  }

  def toggleCompileButton(s: State, method: String): IO[Unit] = {
    Exercises.findByMethod(s, method) match {
      case Some(exercise) ⇒ {
        if (exercise.isFilled)
          enableCompileButton(method)
        else
          disableCompileButton(method)
      }
      case _ ⇒ noop
    }
  }

  def enableCompileButton(method: String): IO[Unit] = {
    DomHandler.nodeByMethod(method)
      .map(s ⇒ s.foreach(
        (node ⇒ DomHandler.enableCompileButton(node).unsafePerformIO())
      ))
  }

  def disableCompileButton(method: String): IO[Unit] = {
    DomHandler.nodeByMethod(method)
      .map(s ⇒ s.foreach(
        (node ⇒ DomHandler.disableCompileButton(node).unsafePerformIO())
      ))
  }

  def startCompilation(s: State, method: String): IO[Unit] = disableCompileButton(method)

  def setAsSolved(s: State, method: String): IO[Unit] = for {
    _ ← disableCompileButton(method)
    _ = println(s"EXERCISE SOLVED $method")
  } yield ()

  def setAsErrored(s: State, method: String): IO[Unit] = for {
    _ ← enableCompileButton(method)
    _ = println(s"EXERCISE ERRORED $method")
  } yield ()
}
