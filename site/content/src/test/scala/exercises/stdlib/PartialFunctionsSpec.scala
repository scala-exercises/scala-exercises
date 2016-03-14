package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class PartialFunctionsSpec extends Spec with Checkers {
  def `partial functions` = {
    check(
      Test.testSuccess(
        PartialFunctions.partialFunctionPartialFunctions _,
        9 :: 8 :: HNil
      )
    )
  }

  def `partial functions with case` = {
    check(
      Test.testSuccess(
        PartialFunctions.caseStatementsPartialFunctions _,
        9 :: 8 :: HNil
      )
    )
  }

  def `andThen chaining` = {
    check(
      Test.testSuccess(
        PartialFunctions.andThenPartialFunctions _,
        14 :: 13 :: HNil
      )
    )
  }

  def `chaining of partial functions` = {
    check(
      Test.testSuccess(
        PartialFunctions.chainOfLogicPartialFunctions _,
        "Odd" :: "Even" :: HNil
      )
    )
  }
}
