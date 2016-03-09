package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class ForExpressionsSpec extends Spec with Checkers {
  def `simple for loops` = {
    check(
      Test.success(
        ForExpressions.forLoopsForExpressions _,
        45 :: HNil
      )
    )
  }

  def `for loop with conditional` = {
    check(
      Test.success(
        ForExpressions.additionalLogicForExpressions _,
        20 :: HNil
      )
    )
  }

  def `nested for expressions` = {
    check(
      Test.success(
        ForExpressions.canBeNestedForExpressions _,
        3 :: 1 :: HNil
      )
    )
  }

  def `readable for expressions` = {
    val result: List[Int] = List(2, 4)

    check(
      Test.success(
        ForExpressions.readableCodeForExpressions _,
        result :: HNil
      )
    )
  }
}
