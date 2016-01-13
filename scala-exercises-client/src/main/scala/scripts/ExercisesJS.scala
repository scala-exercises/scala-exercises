package scripts

import rx._
import rx.ops._
import utils.DomHandler._
import scala.scalajs.js
import shared.IO
import IO._

object ExercisesJS extends js.JSApp {

  object Model {

    def updateExerciseList(ex: List[ClientExercise]): IO[Unit] = io(exercises() = ex)

    case class ClientExercise(method: String, arguments: Seq[String] = Nil) {
      def isFilled: Boolean = !arguments.exists(_.isEmpty) && arguments.nonEmpty
    }

    val exercises = Var(getMethodsList map (m ⇒ ClientExercise(m)) toList)

    exercises foreach (e ⇒ println(e))

    def updateExcercise(method: String, args: Seq[String]): List[ClientExercise] =
      exercises().updated(exerciseIndex(method), ClientExercise(method, args))

    def getExercise(method: String): Option[ClientExercise] = exercises().lift(exerciseIndex(method))

    def exerciseIndex(m: String): Int = exercises().indexWhere(_.method == m)

  }

  import Model._

  def inputChanged(method: String, args: Seq[String]): IO[Unit] =
    updateExerciseList(updateExcercise(method, args))

  def inputBlur(method: String): IO[Unit] = io {
    Model.getExercise(method) foreach (e ⇒ println(e.isFilled))
  }

  def main(): Unit = {

    val program = for {
      _ ← replaceInputs(insertInputs)
      _ ← onInputKeyUp(inputChanged)
      _ ← onInputBlur(inputBlur)
    } yield ()

    program.unsafePerformIO()

  }

}
