/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package state

import model.Exercises._
import actions._

object State {
  def update(s: State, a: Action): State = a match {
    case SetState(newState)           ⇒ newState
    case UpdateExercise(method, args) ⇒ updateByMethod(s, method, args)
    case CompileExercise(method)      ⇒ evaluate(s, method)
    case CompilationOk(method)        ⇒ setAsSolved(s, method)
    case CompilationFail(method, msg) ⇒ setAsErrored(s, method)
    case _                            ⇒ s
  }
}
