package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class ForExpressionsSpec extends Spec with Checkers {
  def `simple for loops` = {
    check(
      Test.testSuccess(
        ForExpressions.forLoopsForExpressions _,
        45 :: HNil
      )
    )
  }

  def `for loop with conditional` = {
    check(
      Test.testSuccess(
        ForExpressions.additionalLogicForExpressions _,
        20 :: HNil
      )
    )
  }

  def `nested for expressions` = {
    check(
      Test.testSuccess(
        ForExpressions.canBeNestedForExpressions _,
        3 :: 1 :: HNil
      )
    )
  }

  def `readable for expressions` = {
    val result: List[Int] = List(2, 4)

    check(
      Test.testSuccess(
        ForExpressions.readableCodeForExpressions _,
        result :: HNil
      )
    )
  }
}
