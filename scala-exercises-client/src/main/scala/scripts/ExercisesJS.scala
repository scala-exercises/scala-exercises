package scripts

import rx._
import rx.ops._
import scripts.ExercisesJS.Model.ClientExercise
import utils.DomHandler._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

object ExercisesJS extends js.JSApp {

  object Model {

    case class ClientExercise(method: String, arguments: Seq[String]) {
      def isFilled = !arguments.exists(_.isEmpty)
    }

    val exercises = Var(List.empty[ClientExercise])

    exercises.foreach(e ⇒ println(e)) //Observer

    def initExercises() = methodsList.foreach(e ⇒ exercises() = ClientExercise(e, inputsMethod(e)) +: exercises())

    def updateExcercise(exercise: ClientExercise) = {
      val pos = exercises().indexWhere(e ⇒ e.method == exercise.method)
      exercises() = exercises().updated(pos, exercise)
    }

  }

  def inputChanged(method: String, args: Seq[String]): Unit = Model.updateExcercise(ClientExercise(method, args))

  def main(): Unit = {

    insertInputs
    activeInputs(inputChanged)
    Model.initExercises

  }

}
