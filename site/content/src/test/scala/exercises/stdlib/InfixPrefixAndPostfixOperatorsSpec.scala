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
      Test.success(
        Operators.singleParameterInfixPrefixandPostfixOperators _,
        7 :: 7 :: HNil
      )
    )
  }

  def `infix operators dont work with two params` = {
    check(
      Test.success(
        Operators.notWithTwoInfixPrefixandPostfixOperators _,
        6 :: 25 :: HNil
      )
    )
  }

  def `postfix operators` = {
    check(
      Test.success(
        Operators.postfixOperatorInfixPrefixandPostfixOperators _,
        "1f" :: HNil
      )
    )
  }

  def `unary operators` = {
    check(
      Test.success(
        Operators.startsWithUnaryInfixPrefixandPostfixOperators _,
        -31 :: HNil
      )
    )
  }

  def `defining your own operators` = {
    check(
      Test.success(
        Operators.ourOwnOperatorInfixPrefixandPostfixOperators _,
        "on" :: "off" :: HNil
      )
    )
  }
}
