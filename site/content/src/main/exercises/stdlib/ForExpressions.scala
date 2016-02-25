package stdlib

import org.scalatest._

/** for_expressions
  *
  */
object ForExpressions extends FlatSpec with Matchers with exercise.Section {


  /** For loops can be simple:
    */
  def forLoopsForExpressions(res0: Int) {
    val someNumbers = Range(0, 10)
    var sum = 0
    for (i <- someNumbers)
      sum += i

    sum should equal(res0)
  }

  /** For loops can contain additional logic:
    */
  def additionalLogicForExpressions(res0: Int) {
    val someNumbers = Range(0, 10)
    var sum = 0

    for (i <- someNumbers)
      if (i % 2 == 0) sum += i

    sum should equal(res0)

  }

  /** For expressions can nest, with later generators varying more rapidly than earlier ones:
    */
  def canBeNestedForExpressions(res0: Int, res1: Int) {
    val xValues = Range(1, 5)
    val yValues = Range(1, 3)
    val coordinates = for {
      x <- xValues
      y <- yValues} yield (x, y)
    coordinates(4) should be(res0, res1)
  }

  /** Using `for` we can make more readable code
    */
  def readableCodeForExpressions(res0: String) {
    val nums = List(List(1), List(2), List(3), List(4), List(5))

    val result = for {
      numList <- nums
      num <- numList
      if (num % 2 == 0)
    } yield (num)

    result should be(res0)

    // Which is the same as
    nums.flatMap(numList => numList).filter(_ % 2 == 0) should be(result)

    // or the same as
    nums.flatten.filter(_ % 2 == 0) should be(result)

  }

}
