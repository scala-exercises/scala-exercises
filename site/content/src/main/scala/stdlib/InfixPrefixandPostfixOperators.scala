package stdlib

import org.scalatest._
import scala.language.postfixOps

/** @param name infix_prefix_and_postfix_operators
  *
  */
object InfixPrefixandPostfixOperators extends FlatSpec with Matchers with exercise.Section {


  /** Any method which takes a single parameter can be used as an infix operator: `a.m(b)` can be written `a m b`.
    */
  def singleParameterInfixPrefixandPostfixOperators(res0: Int, res1: Int) {
    val g: Int = 3
    (g + 4) should be(res0) // + is an infix operator
    g.+(4) should be(res1) // same result but not using the infix operator
  }

  /** Infix Operators do NOT work if an object has a method that takes two parameters:
    */
  def notWithTwoInfixPrefixandPostfixOperators(res0: Int, res1: Int) {
    val g: String = "Check out the big brains on Brad!"

    g indexOf 'o' should be(res0) //indexOf(Char) can be used as an infix operator

    // g indexOf 'o', 4 should be (6) //indexOf(Char, Int) cannot be used as an infix operator

    g.indexOf('o', 7) should be(res1) //indexOf(Char, Int) must use standard java/scala calls
  }

  /** Any method which does not require a parameter can be used as a postfix operator: `a.m` can be written `a m`.
    *
    * For instance `a.##(b)` can be written `a ## b` and `a.!` can be written `a!`
    *
    * **Postfix operators** have lower precedence than **infix operators**, so:
    *    - `foo bar baz` means `foo.bar(baz)`.
    *    - `foo bar baz bam` means `(foo.bar(baz)).bam`
    *    - `foo bar baz bam bim` means `(foo.bar(baz)).bam(bim)`.
    */
  def postfixOperatorInfixPrefixandPostfixOperators(res0: String) {
    val g: Int = 31
    (g toHexString) should be(res0) //toHexString takes no params therefore can be called as a postfix operator.
    //Hint: The answer is "1f"
  }

  /** Prefix operators work if an object has a method name that starts with `unary_` :
    */
  def startsWithUnaryInfixPrefixandPostfixOperators(res0: Int) {
    val g: Int = 31
    (-g) should be(res0)
  }

  /** Here we create our own prefix operator for our own class. The only identifiers that can be used as prefix operators are `+`, `-`, `!`, and `~`:
    */
  def ourOwnOperatorInfixPrefixandPostfixOperators(res0: String, res1: String) {
    class Stereo {
      def unary_+ = "on"

      def unary_- = "off"
    }

    val stereo = new Stereo
    (+stereo) should be(res0)
    (-stereo) should be(res1)
  }

}
