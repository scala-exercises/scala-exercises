package ui

import shared.IO
import IO._

import scala.scalajs.js
import js.DynamicImplicits._
import js.Dynamic.{ global => g }

import model._
import model.Exercises._
import utils.{ DomHandler }

object UI {
  def noop: IO[Unit] = io {}

  def toggleCompileButton(s: State, method: String): IO[Unit] = {
    println("TOGGLING COMPILE BUTTON!")
    Exercises.findByMethod(s, method) match {
      case Some(exercise) ⇒ {
        println("FOUND EX! " + exercise)
        if (exercise.isFilled)
          enableCompileButton(method)
        else
          disableCompileButton(method)
      }
      case _ => noop
    }
  }

  def enableCompileButton(method: String): IO[Unit] = {
    DomHandler.nodeByMethod(method)
      .map(s => s.foreach(
        (node => DomHandler.enableCompileButton(node).unsafePerformIO())
      ))
  }

  def disableCompileButton(method: String): IO[Unit] = {
    DomHandler.nodeByMethod(method)
      .map(s => s.foreach(
        (node => DomHandler.disableCompileButton(node).unsafePerformIO())
      ))
  }
}
