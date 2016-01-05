import rx._
import rx.ops._

val l = List(Seq(""), Seq("", ""), Seq("", "", ""))


case class ClientExercise(method: String, arguments: Seq[String]){

  def setArgs(args: Seq[String]) = copy(arguments = args)

  def isFilled = !arguments.exists(_.isEmpty)

  def evaluate = println("Cambia " + method)
}

val exercises = Var(List.empty[ClientExercise])
exercises.foreach(e => println(e))



def initExercises() = l.zipWithIndex foreach { case(e, i) =>
  exercises() = ClientExercise("Met"+i, e) +: exercises()
}

initExercises()


def updateExcercise(exercise: ClientExercise) = {
  val pos = exercises().indexWhere(e => e.method == exercise.method)
  exercises() = exercises().updated(pos, exercise)
}


updateExcercise(ClientExercise("Met0", Seq("Rafa")))










