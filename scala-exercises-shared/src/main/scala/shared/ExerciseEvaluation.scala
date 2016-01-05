package shared

/** Input params necessary to evaluate an exercise
  */
case class ExerciseEvaluation(
    section: String,
    category: String,
    method: String,
    args: List[String])

/** Represents the response of exercises
  */
case class Argument(
    value: String,
    status: Boolean)