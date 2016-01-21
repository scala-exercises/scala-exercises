package factories

import model._

object Factories {
  def clientExercise(
    library: String        = "",
    section: String        = "",
    method:  String        = "",
    args:    Seq[String]   = Seq(),
    state:   ExerciseState = Unsolved
  ): ClientExercise =
    ClientExercise(library = library, section = section, method = method, arguments = args, state = state)
}
