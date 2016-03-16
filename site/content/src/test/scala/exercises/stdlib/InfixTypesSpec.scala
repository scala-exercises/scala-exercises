package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class InfixTypesSpec extends Spec with Checkers {
  def `infix type` = {
    check(
      Test.testSuccess(
        InfixTypes.infixTypeInfixTypes _,
        "Romeo is in love with Juliet" :: HNil
      )
    )
  }

  def `infix type operators` = {
    check(
      Test.testSuccess(
        InfixTypes.infixOperatorInfixTypes _,
        "Romeo is in love with Juliet" :: HNil
      )
    )
  }
}
