package model

case class ClientExercise(
    method:    String,
    arguments: Seq[String] = Nil
) {

  def isFilled: Boolean = !arguments.exists(_.isEmpty) && arguments.nonEmpty

  def isSolved: Boolean = false

  def canBeCompiled: Boolean = isFilled && !isSolved
}

class ExerciseState extends Enumeration {
  val Unsolved, Evaluating, Errored, Solved = Value
}

object Exercises {
  type State = List[ClientExercise]

  def findByMethod(s: State, method: String): Option[ClientExercise] =
    s.find(_.method == method)

  def updateByMethod(s: State, method: String, args: Seq[String]): State =
    s.map(ex ⇒ {
      if (ex.method == method)
        ex.copy(arguments = args)
      else
        ex
    })
}
