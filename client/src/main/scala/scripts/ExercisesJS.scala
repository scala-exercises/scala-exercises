/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
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
 */

package org.scalaexercises.client
package scripts

import monix.eval.Coeval
import monix.execution.Scheduler.Implicits.{global => scheduler}
import monix.reactive._
import monix.reactive.subjects._
import org.scalaexercises.client.actions._
import org.scalaexercises.client.effects.Effects
import org.scalaexercises.client.model.Exercises._
import org.scalaexercises.client.state.State
import org.scalaexercises.client.ui.UI
import org.scalaexercises.client.utils.DomHandler._

import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ExercisesJS")
object ExercisesJS {
  @JSExport
  def main(args: Array[String]): Unit = {

    // A subject where every program action is published
    val actions = BehaviorSubject[Action](Start)

    // State is the reduction of the initial state and the stream of actions
    val state: Observable[State] = actions.scan(Nil: State)(State.update)
    // UI modifications
    val ui: Observable[Coeval[Unit]] = Observable.zipMap2(state, actions)(UI.update)

    // Effects that can trigger further actions
    val effects: Observable[Future[Option[Action]]] =
      Observable.zipMap2(state, actions)(Effects.perform)

    def triggerAction(action: Action): Coeval[Unit] =
      Coeval {
        actions.onNext(action)
      }.void

    def wireObservables: Coeval[Unit] =
      Coeval {
        ui.foreach(ioAction => ioAction.value)
        effects.foreach { (f: Future[Option[Action]]) =>
          f.foreach(m => m.foreach(a => actions.onNext(a)))
        }
      }.void

    def startInteraction: Coeval[Unit] = {
      for {
        _ <- inputReplacements flatMap replaceInputs
        _ <- onInputKeyUp(
          (method: String, arguments: Seq[String]) =>
            triggerAction(UpdateExercise(method, arguments)),
          (method: String) => triggerAction(CompileExercise(method))
        )
        _ <- onButtonClick((method: String) => triggerAction(CompileExercise(method)))
        _ <- onInputChange { (method: String, arguments: Seq[String]) =>
          triggerAction(UpdateExercise(method, arguments))
        }
      } yield ()
    }

    val program = for {
      _ <- wireObservables
      _ <- highlightCodeBlocks
      _ <- emojify
      _ <- startInteraction
    } yield ()

    program.value
  }
}
