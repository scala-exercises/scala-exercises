/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.scalaexercises.client
package scripts

import utils.DomHandler._

import scala.scalajs.js
import scala.concurrent.Future

import monix.cats._
import monix.eval.Coeval

import model._
import model.Exercises._
import actions._
import ui.UI
import state.State
import effects.Effects

import monix.reactive._
import monix.reactive.subjects._
import monix.execution.Scheduler.Implicits.{global ⇒ scheduler}

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
    val ui: Observable[Coeval[Unit]] = Observable.zipMap2(state, actions)(UI.update _)

    // Effects that can trigger further actions
    val effects: Observable[Future[Option[Action]]] =
      Observable.zipMap2(state, actions)(Effects.perform _)

    def triggerAction(action: Action): Coeval[Unit] = Coeval {
      actions.onNext(action)
    }

    def wireObservables: Coeval[Unit] = Coeval {
      ui.foreach(ioAction ⇒ {
        ioAction.value
      })
      effects.foreach((f: Future[Option[Action]]) ⇒ {
        f.foreach(m ⇒ {
          m.foreach(a ⇒ actions.onNext(a))
        })
      })
    }

    def startInteraction: Coeval[Unit] = {
      for {
        _ ← inputReplacements flatMap replaceInputs
        _ ← onInputKeyUp((method: String, arguments: Seq[String]) ⇒ {
          triggerAction(UpdateExercise(method, arguments))
        }, (method: String) ⇒ {
          triggerAction(CompileExercise(method))
        })
        _ ← onButtonClick((method: String) ⇒ {
          triggerAction(CompileExercise(method))
        })
        _ ← onInputChange((method: String, arguments: Seq[String]) ⇒ {
          triggerAction(UpdateExercise(method, arguments))
        })
      } yield ()
    }

    val program = for {
      _ ← wireObservables
      _ ← highlightCodeBlocks
      _ ← emojify
      _ ← startInteraction
    } yield ()

    program.value
  }
}
