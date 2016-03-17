package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class OperatorsSpec extends Spec with Checkers {
  val Operators = InfixPrefixandPostfixOperators

  def `single parameter infix operators` = {
    check(
      Test.testSuccess(
        Operators.singleParameterInfixPrefixandPostfixOperators _,
        7 :: 7 :: HNil
      )
    )
  }

  def `infix operators dont work with two params` = {
    check(
      Test.testSuccess(
        Operators.notWithTwoInfixPrefixandPostfixOperators _,
        6 :: 25 :: HNil
      )
    )
  }

  def `postfix operators` = {
    check(
      Test.testSuccess(
        Operators.postfixOperatorInfixPrefixandPostfixOperators _,
        "1f" :: HNil
      )
    )
  }

  def `unary operators` = {
    check(
      Test.testSuccess(
        Operators.startsWithUnaryInfixPrefixandPostfixOperators _,
        -31 :: HNil
      )
    )
  }

  def `defining your own operators` = {
    check(
      Test.testSuccess(
        Operators.ourOwnOperatorInfixPrefixandPostfixOperators _,
        "on" :: "off" :: HNil
      )
    )
  }
}
