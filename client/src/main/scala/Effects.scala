/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package effects

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import utils.DomHandler
import actions._
import model._
import model.Exercises._
import api.Client

object Effects {
  def noop: Future[Option[Action]] = Future(None)

  def perform(s: State, a: Action): Future[Option[Action]] = a match {
    case Start                   ⇒ loadInitialData
    case CompileExercise(method) ⇒ compileExercise(s, method)
    case _                       ⇒ noop
  }

  def loadInitialData: Future[Option[Action]] = {
    DomHandler.libraryAndSection.fold(Future(None): Future[Option[Action]])(libAndSection ⇒ {
      val (lib, sect) = libAndSection
      Client.fetchProgress(lib, sect).collect({
        case Some(state) ⇒ Some(SetState(state))
      })
    })
  }

  def compileExercise(s: State, method: String): Future[Option[Action]] =
    findByMethod(s, method) match {
      case Some(exercise) if exercise.isFilled ⇒ Client.compileExercise(exercise).map(result ⇒ {
        if (result.ok)
          Some(CompilationOk(result.method))
        else
          Some(CompilationFail(result.method, result.msg))
      })
      case _ ⇒ noop
    }

}
