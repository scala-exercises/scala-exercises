package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class PartiallyAppliedFunctionsSpec extends Spec with Checkers {
  def `partially applied functions` = {
    check(
      Test.success(
        PartiallyAppliedFunctions.partiallyAppliedPartiallyAppliedFunctions _,
        17 :: 15 :: HNil
      )
    )
  }

  def `accept any number of arguments` = {
    check(
      Test.success(
        PartiallyAppliedFunctions.anyNumberArgumentsPartiallyAppliedFunctions _,
        15 :: 15 :: HNil
      )
    )
  }

  def `currying` = {
    check(
      Test.success(
        PartiallyAppliedFunctions.curryingPartiallyAppliedFunctions _,
        true :: 20 :: 6 :: 8 :: 16 :: HNil
      )
    )
  }

  def `specialization` = {
    check(
      Test.success(
        PartiallyAppliedFunctions.specializedVersionPartiallyAppliedFunctions _,
        List(12, 20, 2) :: List(11, 5, 3, 13) :: HNil
      )
    )
  }
}
