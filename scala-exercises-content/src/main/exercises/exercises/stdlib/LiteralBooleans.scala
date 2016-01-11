package exercises.stdlib

import org.scalatest._

/**
  */
class LiteralBooleans extends FlatSpec with Matchers with exercise.Section {

  /** Boolean literals are either true or false, using the true or false keyword
    */
  def literalBooleans(res0: String, res1: String, res2: String, res3: String, res4: String, res5: String): ExerciseResult[Unit] = ExerciseRunner("Literal Booleans") {
    val a = true
    val b = false
    val c = 1 > 2
    val d = 1 < 2
    val e = a == c
    val f = b == d
    a should be()
    b should be()
    c should be()
    d should be()
    e should be()
    f should be()

  }(∞)

}
