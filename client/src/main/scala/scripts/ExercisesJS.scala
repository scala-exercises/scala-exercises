/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package scripts

import utils.DomHandler._

import scala.scalajs.js
import scala.concurrent.Future

import fp.IO
import IO._
import model._
import model.Exercises._
import actions._
import ui.UI
import state.State
import effects.Effects

import monix.reactive._
import monix.reactive.subjects._
import monix.execution.Scheduler.Implicits.{ global ⇒ scheduler }

object ExercisesJS extends js.JSApp {
  def main(): Unit = {
    // A subject where every program action is published
    val actions = BehaviorSubject[Action](Start)

    // State is the reduction of the initial state and the stream of actions
    val state: Observable[State] = actions.scan(Nil: State)(State.update)

    // A stream of (State, Action) pairs that emits a value each time the state is affected
    // by an action
    val stateAndAction: Observable[(State, Action)] = state.zip(actions)

    // UI modifications
    val ui: Observable[IO[Unit]] = state.zipWith(actions)(UI.update)

    // Effects that can trigger further actions
    val effects: Observable[Future[Option[Action]]] = state.zipWith(actions)(Effects.perform)

    def triggerAction(action: Action): IO[Unit] = io {
      actions.onNext(action)
    }

    def wireObservables: IO[Unit] = io {
      ui.foreach(ioAction ⇒ {
        ioAction.unsafePerformIO()
      })
      effects.foreach((f: Future[Option[Action]]) ⇒ {
        f.foreach(m ⇒ {
          m.foreach(a ⇒ actions.onNext(a))
        })
      })
    }

    def startInteraction: IO[Unit] = {
      for {
        _ ← onInputKeyUp((method: String, arguments: Seq[String]) ⇒ {
          triggerAction(UpdateExercise(method, arguments))
        }, (method: String) ⇒ {
          triggerAction(CompileExercise(method))
        })
        _ ← onButtonClick((method: String) ⇒ {
          triggerAction(CompileExercise(method))
        })
      } yield ()
    }

    val program = for {
      _ ← wireObservables
      _ ← highlightCodeBlocks
      _ ← emojify
      _ ← startInteraction
    } yield ()

    program.unsafePerformIO()
  }
}
