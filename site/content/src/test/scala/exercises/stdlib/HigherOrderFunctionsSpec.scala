package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class HigherOrderFunctionsSpec extends Spec with Checkers {
  def `anonymous function` = {
    check(
      Test.success(
        HigherOrderFunctions.meetLambdaHigherOrderFunctions _,
        4 :: 4 :: 4 :: 4 :: 4 :: 4 :: HNil
      )
    )
  }

  def `anonymous function with different syntax` = {
    check(
      Test.success(
        HigherOrderFunctions.differentLookHigherOrderFunctions _,
        6 :: HNil
      )
    )
  }

  def `anonymous function with closure` = {
    check(
      Test.success(
        HigherOrderFunctions.meetClosureHigherOrderFunctions _,
        11 :: 12 :: HNil
      )
    )
  }

  def `anonymous functions hold the environment` = {
    check(
      Test.success(
        HigherOrderFunctions.holdEnvironmentHigherOrderFunctions _,
        13 :: 14 :: HNil
      )
    )
  }

  def `returning functions` = {
    check(
      Test.success(
        HigherOrderFunctions.returningFunctionHigherOrderFunctions _,
        true :: 5 :: 10 :: HNil
      )
    )
  }

  def `returning anonymous functions` = {
    check(
      Test.success(
        HigherOrderFunctions.returningAnonymousFunctionHigherOrderFunctions _,
        true :: 5 :: 10 :: HNil
      )
    )
  }

  def `is instance of method` = {
    check(
      Test.success(
        HigherOrderFunctions.isInstanceOfMethodHigherOrderFunctions _,
        true :: HNil
      )
    )
  }

  def `function as a parameter` = {
    check(
      Test.success(
        HigherOrderFunctions.functionAsParameterHigherOrderFunctions _,
        List("ABC", "XYZ", "123") :: List("abc", "xyz", "123") :: List(5, 6, 7) :: HNil
      )
    )
  }
}
