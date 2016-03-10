package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class InfixTypesSpec extends Spec with Checkers {
  def `infix type` = {
    check(
      Test.success(
        InfixTypes.infixTypeInfixTypes _,
        "Romeo is in love with Juliet" :: HNil
      )
    )
  }

  def `infix type operators` = {
    check(
      Test.success(
        InfixTypes.infixOperatorInfixTypes _,
        "Romeo is in love with Juliet" :: HNil
      )
    )
  }
}
