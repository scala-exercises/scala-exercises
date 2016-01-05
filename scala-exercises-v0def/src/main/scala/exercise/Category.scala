package exercise

import scalaz.\/

/** Marker trait for exercise categories.
  */
trait Category {
  object ∞

  type ExerciseResult[A] = Throwable \/ A

  object ExerciseRunner {
    def apply[A](title: String)(exercise: ⇒ A)(end: ∞.type): ExerciseResult[A] = \/.fromTryCatchNonFatal(exercise)
  }

}
