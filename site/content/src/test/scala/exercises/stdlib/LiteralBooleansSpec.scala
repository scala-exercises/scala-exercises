package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class LiteralBooleansSpec extends Spec with Checkers {
  def `are either true or false` = {
    check(
      Test.testSuccess(
        LiteralBooleans.literalBooleanLiteralBooleans _,
        true :: false :: false :: true :: false :: false :: HNil
      )
    )
  }
}
