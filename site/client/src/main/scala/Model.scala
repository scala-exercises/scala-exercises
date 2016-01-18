package model

sealed trait ExerciseState
case object Unsolved extends ExerciseState
case object Evaluating extends ExerciseState
case object Errored extends ExerciseState
case object Solved extends ExerciseState

case class ClientExercise(
  method:    String,
  arguments: Seq[String] = Nil,
  state: ExerciseState = Unsolved
) {

  def isFilled: Boolean = !arguments.exists(_.isEmpty) && arguments.nonEmpty

  def isSolved: Boolean = state == Solved

  def isBeingEvaluated: Boolean = state == Evaluating

  def canBeCompiled: Boolean = isFilled && !isSolved && !isBeingEvaluated
}


object Exercises {
  type State = List[ClientExercise]

  def findByMethod(s: State, method: String): Option[ClientExercise] =
    s.find(_.method == method)

  def applyByMethod(s: State, method: String, f: ClientExercise => ClientExercise): State =
    s.map(ex ⇒ {
      if (ex.method == method)
        f(ex)
      else
        ex
    })

  def updateByMethod(s: State, method: String, args: Seq[String]): State =
    applyByMethod(s, method, _.copy(arguments=args))

}

