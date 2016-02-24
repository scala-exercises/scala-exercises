package stdlib

import org.scalatest._

/** partially_applied_functions
  *
  */
object PartiallyAppliedFunctions extends FlatSpec with Matchers with exercise.Section {


  /** partiallyAppliedPartiallyAppliedFunctions
    *
    * A partially applied function is a function that you do not apply any or all the arguments, creating another function. This partially applied function doesn't apply any arguments.
    */
  def partiallyAppliedPartiallyAppliedFunctions(res0: Int, res1: Int) {
    def sum(a: Int, b: Int, c: Int) = a + b + c
    val sum3 = sum _
    sum3(1, 9, 7) should be(res0)
    sum(4, 5, 6) should be(res1)
  }

  /** anyNumberArgumentsPartiallyAppliedFunctions
    *
    * Partially applied functions can replace any number of arguments:
    */
  def anyNumberArgumentsPartiallyAppliedFunctions(res0: Int, res1: Int) {
    def sum(a: Int, b: Int, c: Int) = a + b + c
    val sumC = sum(1, 10, _: Int)
    sumC(4) should be(res0)
    sum(4, 5, 6) should be(res1)
  }

  /** curryingPartiallyAppliedFunctions
    *
    * Currying is a technique to transform function with multiple parameters into multiple functions which each take one parameter
    */
  def curryingPartiallyAppliedFunctions(res0: Boolean, res1: Int, res2: Int, res3: Int, res4: Int) {
    def multiply(x: Int, y: Int) = x * y
    (multiply _).isInstanceOf[Function2[_, _, _]] should be(res0)
    val multiplyCurried = (multiply _).curried
    multiply(4, 5) should be(res1)
    multiplyCurried(3)(2) should be(res2)
    val multiplyCurriedFour = multiplyCurried(4)
    multiplyCurriedFour(2) should be(res3)
    multiplyCurriedFour(4) should be(res4)
  }

  /** specializedVersionPartiallyAppliedFunctions
    *
    * Currying allows you to create specialized version of generalized function
    */
  def specializedVersionPartiallyAppliedFunctions(res0: String, res1: String) {
    def customFilter(f: Int => Boolean)(xs: List[Int]) = {
      xs filter f
    }
    def onlyEven(x: Int) = x % 2 == 0
    val xs = List(12, 11, 5, 20, 3, 13, 2)
    customFilter(onlyEven)(xs) should be(res0)

    val onlyEvenFilter = customFilter(onlyEven) _
    onlyEvenFilter(xs) should be(res1)
  }

}