package stdlib

import org.scalatest._

/** Literal Booleans
  *
  * Literal Booleans Description
  */
object LiteralBooleans extends FlatSpec with Matchers with exercise.Section {


  /** litealBooleanLiteralBooleans
    *
    * Boolean literals are either true or false, using the true or false keyword
    */
  def litealBooleanLiteralBooleans(res0: String, res1: String, res2: String, res3: String, res4: String, res5: String) {
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
  }

}