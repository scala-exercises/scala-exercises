package stdlib

import org.scalatest._

/** Range
  *
  * Range Description
  */

object RangeExercises extends FlatSpec with Matchers with exercise.Section {


  /** upperNotInclusiveRange
    *
    * A range's upper bound is not inclusive:
    */
  def upperNotInclusiveRange(res0: String, res1: String, res2: String) {
    val someNumbers = Range(0, 10)
    val second = someNumbers(1)
    val last = someNumbers.last

    someNumbers.size should be(res0)
    second should be(res1)
    last should be(res2)


  }

  /** unitlRange
    *
    * Ranges can be specified using 'until'.
    */
  def unitlRange(res0: String) {
    val someNumbers = Range(0, 10)
    val otherRange = 0 until 10

    (someNumbers == otherRange) should be(res0)


  }

  /** incrementsRange
    *
    * Range can specify a step for an increment:
    */
  def incrementsRange(res0: String, res1: String, res2: String) {
    val someNumbers = Range(2, 10, 3)
    val second = someNumbers(1)
    val last = someNumbers.last

    someNumbers.size should be(res0)
    second should be(res1)
    last should be(res2)


  }

  /** upperInIncrementRange
    *
    * A range does not include its upper bound, even in a step increment:
    */
  def upperInIncrementRange(res0: String, res1: String, res2: String) {
    val someNumbers = Range(0, 34, 2)
    someNumbers.contains(33) should be(res0)
    someNumbers.contains(32) should be(res1)
    someNumbers.contains(34) should be(res2)


  }

  /** specifyUpperRange
    *
    * Range can specify to include its upper bound value
    */
  def specifyUpperRange(res0: String) {
    val someNumbers = Range(0, 34).inclusive

    someNumbers.contains(34) should be(res0)


  }

  /** inclusiveWithToRange
    *
    * Inclusive ranges can be specified using 'to'.
    */
  def inclusiveWithToRange(res0: String) {
    val someNumbers = Range(0, 34).inclusive
    val otherRange = 0 to 34

    (someNumbers == otherRange) should be(res0)

    /** A Range is an ordered sequence of integers that are equally spaced apart. For example, "1, 2, 3," is a range, as is "5, 8, 11, 14." To create a range in Scala, use the predefined methods `to` and `by`. `1 to 3` generates `Range(1, 2, 3)` and `5 to 14 by 3` generates `Range(5, 8, 11, 14)`.
      *
      * If you want to create a range that is exclusive of its upper limit, then use the convenience method `until` instead of `to`: `1 until 3` generates `Range(1, 2)`.
      *
      * Ranges are represented in constant space, because they can be defined by just three numbers: their start, their end, and the stepping value. Because of this representation, most operations on ranges are extremely fast.
      * */
  }

}