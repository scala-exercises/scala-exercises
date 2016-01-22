package exercises.stdlib

import org.scalatest._

/**
  */
class LiteralBooleans extends FlatSpec with Matchers with exercise.Section {

  /** Boolean literals are either true or false, using the true or false keyword
    */
  def literalBooleans(res0: Boolean, res1: Boolean, res2: Boolean, res3: Boolean, res4: Boolean, res5: Boolean): ExerciseResult[Unit] = ExerciseRunner("Literal Booleans") {
    val a = true
    val b = false
    val c = 1 > 2
    val d = 1 < 2
    val e = a == c
    val f = b == d
    a should be(res0)
    b should be(res1)
    c should be(res2)
    d should be(res3)
    e should be(res4)
    f should be(res5)

  }(∞)

}
