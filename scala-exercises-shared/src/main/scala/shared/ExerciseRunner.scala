package shared

import scalaz.\/

/**
 * All Exercises should return ExerciseResult[Unit] until we find a valid use case for other result types and end
 * for parsing purposes with `}(∞)`
 */
object ExerciseRunner {

  type ExerciseResult[A] = Throwable \/ A

  object ∞

  def apply[A](title : String)(exercise: => A)(end : ∞.type) : ExerciseResult[A] = \/.fromTryCatchNonFatal(exercise)

}
