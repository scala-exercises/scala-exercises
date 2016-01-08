package scripts

import rx._
import rx.ops._
import utils.DomHandler._
import scala.scalajs.js

object ExercisesJS extends js.JSApp {

  object Model {

    case class ClientExercise(method: String, arguments: Seq[String]) {
      def isFilled = !arguments.exists(_.isEmpty) && arguments.nonEmpty
    }

    val exercises = Var(getMethodsList map (addExercise(_)) toList)

    exercises.foreach(e ⇒ println(e)) //Observer

    def addExercise(method: String, args: Seq[String] = Nil): ClientExercise = ClientExercise(method, args)

    def updateExcercise(method: String, args: Seq[String]) = exercises() = exercises().updated(position(method), addExercise(method, args))

    def getExercise(method: String): Option[ClientExercise] = exercises().lift(position(method))

    def position(m: String): Int = exercises().indexWhere(_.method == m)

  }

  def inputChanged(method: String, args: Seq[String]): Unit = Model.updateExcercise(method, args)

  def inputBlur(method: String): Unit = for {
    exercise ← Model.getExercise(method)
  } yield println(exercise.isFilled)

  def main(): Unit = {

    insertInputs
    activeInputs(inputChanged, inputBlur)

  }

}
