/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package scripts

import rx._
import utils.DomHandler._

import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global

import shared.IO
import IO._
import model._
import model.Exercises._
import actions._
import ui.UI
import state.State
import effects.Effects

import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.reactive._
import monix.reactive.subjects._

import monix.execution.Scheduler.Implicits.global

object ExercisesJS extends js.JSApp {
  def main(): Unit = {
    val actionsSub = BehaviorSubject[Action](Start)

    actionsSub.subscribe(new Observer[Action] {
      def onNext(elem: Action) = {
        println("[ACTION]: " + elem)
        Continue
      }

      def onError(ex: Throwable) = ()
      def onComplete() = ()
    })

    val states: Var[State] = Var(Nil)
    val actions: Var[Action] = Var(Start)

    def setState(s: State): IO[Unit] = io {
      states() = s
    }

    def triggerAction(action: Action): IO[Unit] = {
      actions() = action
      val oldState = states()
      val newState = State.update(oldState, action)
      setState(newState)
    }

    Obs(states) {
      val state = states()
      val action = actions()

      // Perform effects
      Effects.perform(state, action).foreach(m ⇒ {
        m.foreach(triggerAction(_).unsafePerformIO())
      })
      // Update UI
      UI.update(state, action).unsafePerformIO()
    }

    def startInteraction: IO[Unit] = for {
      _ ← onInputKeyUp((method: String, arguments: Seq[String]) ⇒ {
        triggerAction(UpdateExercise(method, arguments))
      }, (method: String) ⇒ {
        triggerAction(CompileExercise(method))
      })
      _ ← onButtonClick((method: String) ⇒ {
        triggerAction(CompileExercise(method))
      })
      _ ← onInputBlur((method: String) ⇒ {
        triggerAction(CompileExercise(method))
      })
    } yield ()

    val program = for {
      _ ← highlightCodeBlocks
      _ ← startInteraction
    } yield ()

    program.unsafePerformIO()
  }
}
