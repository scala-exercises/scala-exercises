/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package effects

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import actions._
import model.Exercises._
import api.Client

object Effects {
  def noop: Future[Option[Action]] = Future(None)

  def perform(s: State, a: Action): Future[Option[Action]] = a match {
    case CompileExercise(method) ⇒ {
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
    case _ ⇒ noop
  }
}
