package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class RangesSpec extends Spec with Checkers {
  def `upper bounds are not inclusive` = {
    check(
      Test.testSuccess(
        Ranges.upperNotInclusiveRangeExercises _,
        10 :: 1 :: 9 :: HNil
      )
    )
  }

  def `until for creating ranges` = {
    check(
      Test.testSuccess(
        Ranges.untilRangeExercises _,
        true :: HNil
      )
    )
  }

  def `ranges with step` = {
    check(
      Test.testSuccess(
        Ranges.incrementsRangeExercises _,
        3 :: 5 :: 8 :: HNil
      )
    )
  }

  def `ranges with step dont include upper bound` = {
    check(
      Test.testSuccess(
        Ranges.upperInIncrementRangeExercises _,
        false :: true :: false :: HNil
      )
    )
  }

  def `ranges which include upper bound` = {
    check(
      Test.testSuccess(
        Ranges.specifyUpperRangeExercises _,
        true :: HNil
      )
    )
  }

  def `ranges which include upper bound with to` = {
    check(
      Test.testSuccess(
        Ranges.inclusiveWithToRangeExercises _,
        true :: HNil
      )
    )
  }
}
