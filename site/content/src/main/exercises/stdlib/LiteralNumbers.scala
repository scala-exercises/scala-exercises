package stdlib

import org.scalatest._

/** Literal Numbers
  *
  * Literal Numbers Description
  */
object LiteralNumbers extends FlatSpec with Matchers with exercise.Section {


  /** integerLiteralsLiteralNumbers
    *
    * Integer Literals are 32-bit and can be created from decimal, hexadecimal:
    */
  def integerLiteralsLiteralNumbers(res0: String, res1: String, res2: String, res3: String, res4: String, res5: String, res6: String) {
    val a = 2
    val b = 31
    val c = 0x30F
    val e = 0
    val f = -2
    val g = -31
    val h = -0x30F
    a should be(res0)
    b should be(res1)
    c should be(res2) //Hint: 30F = 783
    e should be(res3)
    f should be(res4)
    g should be(res5)
    h should be(res6) //Hint: 30F = 783
  }

  /** longLiteralsLiteralNumbers
    *
    * Long Literals are 64 bit, are specified by appending an `L` or `l` at the end:
    */
  def longLiteralsLiteralNumbers(res0: String, res1: String, res2: String, res3: String, res4: String, res5: String, res6: String) {
    val a = 2L
    val b = 31L
    val c = 0x30FL
    val e = 0L
    val f = -2l
    val g = -31L
    val h = -0x30FL

    a should be(res0)
    b should be(res1)
    c should be(res2) //Hint: 30F = 783
    e should be(res3)
    f should be(res4)
    g should be(res5)
    h should be(res6) //Hint: 30F = 783
  }

  /** floatsAndDoublesLiteralNumbers
    *
    * Float and Double Literals are IEEE 754 for specific, Float are 32-bit length, Doubles are 64-bit.
    * Floats can be coerced using a f or F suffix, and Doubles can be coerced using a d or D suffix.
    * Exponent are specified using e or E.
    */
  def floatsAndDoublesLiteralNumbers(res0: String, res1: String, res2: String, res3: String, res4: String, res5: String, res6: String, res7: String, res8: String) {
    val a = 3.0
    val b = 3.00
    val c = 2.73
    val d = 3f
    val e = 3.22d
    val f = 93e-9
    val g = 93E-9
    val h = 0.0
    val i = 9.23E-9D

    a should be(res0)
    b should be(res1)
    c should be(res2)
    d should be(res3)
    e should be(res4)
    f should be(res5)
    g should be(res6)
    h should be(res7)
    i should be(res8)
  }

}