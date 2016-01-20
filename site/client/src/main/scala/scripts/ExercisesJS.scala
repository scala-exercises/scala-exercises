package scripts

import rx._
import rx.ops._
import utils.DomHandler._

import scala.scalajs.js
import scala.concurrent.{ Future }
import scala.concurrent.ExecutionContext.Implicits.global

import shared.IO
import IO._
import model._
import model.Exercises._
import actions._

object Ajax {
  def compileExercise(e: ClientExercise): Future[Option[Action]] =
    Future(Some(NoOp()))
}

object Program {
  def updateState(s: State, a: Action): State = a match {
    case SetState(newState) => newState
    case UpdateExercise(method, args) ⇒ updateByMethod(s, method, args)
    case _                            ⇒ s
  }
  def runEffect(s: State, a: Action): Future[Option[Action]] = a match {
    case CompileExercise(method) ⇒ {
      findByMethod(s, method) match {
        case Some(exercise) if exercise.canBeCompiled ⇒ Ajax.compileExercise(exercise)
        case _                                        ⇒ Future(None)
      }
    }
    case _ ⇒ Future(None)
  }
  def updateUI(s: State, a: Action): IO[Unit] = a match {
    case UpdateExercise(method, args) ⇒ io { println("Exercise updated") }
    case CompileExercise(method)      ⇒ io { println("Exercise being compiled") }
    case _                            ⇒ io {}
  }
}

object ExercisesJS extends js.JSApp {

  def loadInitialData: IO[State] = io {
    getMethodsList map (m ⇒ ClientExercise(m)) toList
  }

  def main(): Unit = {
    val states: Var[State] = Var(Nil)
    val actions: Var[Action] = Var(NoOp())

    def setState(s: State): IO[Unit] = io {
      states() = s
    }

    def triggerAction(action: Action): IO[Unit] = io {
      actions() = action
      val oldState = states()
      val newState = Program.updateState(oldState, action)
      setState(newState).unsafePerformIO()
    }

    val effects = Obs(states, skipInitial = true) {
      Program.runEffect(states(), actions()).foreach(m ⇒ {
        m.foreach(triggerAction(_))
      })
    }
    val ui = Obs(states, skipInitial = true) {
      Program.updateUI(states(), actions()).unsafePerformIO()
    }

    val program = for {
      initialState ← loadInitialData flatMap setState
      // TODO: reflect initial state in DOM
      _ ← replaceInputs(insertInputs)
      _ ← onInputKeyUp((method: String, arguments: Seq[String]) ⇒ {
        triggerAction(UpdateExercise(method, arguments))
      })
      _ ← onInputBlur((method: String) ⇒ {
        triggerAction(CompileExercise(method))
      })
    } yield ()

    program.unsafePerformIO()
  }
}
