package scripts

import rx._
import rx.ops._
import utils.DomHandler._
import scala.scalajs.js

object ExercisesJS extends js.JSApp {

  object Model {

    case class ClientExercise(method: String, arguments: Seq[String]) {
      def isFilled = !arguments.exists(_.isEmpty)
    }

    val exercises = Var(methodsList.flatMap(addExercise(_)).toList)

    exercises.foreach(e ⇒  println(e)) //Observer

    def addExercise(method: Option[String], args: Seq[Option[String]] = Nil): Option[ClientExercise] = method.map(m => ClientExercise(m, args.map(_.getOrElse(""))))

    def updateExcercise(method: Option[String], args: Seq[Option[String]]) = addExercise(method, args).map(e => exercises() = exercises().updated(position(e), e))

    def position(e: ClientExercise): Int = exercises().indexWhere(_.method == e.method)

  }

  def inputChanged(method: Option[String], args: Seq[Option[String]]): Unit = Model.updateExcercise(method, args)

  def main(): Unit = {

    insertInputs
    activeInputs(inputChanged)
    Model.initExercises

  }

}
