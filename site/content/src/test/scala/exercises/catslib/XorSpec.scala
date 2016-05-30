package exercises

import catslib._
import shapeless.HNil
import cats.data.Xor
import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class XorSpec extends Spec with Checkers {
  def `is right biased` = {
    check(
      Test.testSuccess(
        XorSection.xorMapRightBias _,
        Xor.right[String, Int](6) :: Xor.left[String, Int]("Something went wrong") :: HNil
      )
    )
  }

  def `has a Monad implementation` = {
    check(
      Test.testSuccess(
        XorSection.xorMonad _,
        Xor.right[String, Int](6) :: Xor.left[String, Int]("Something went wrong") :: HNil
      )
    )
  }

  def `Xor instead of exceptions` = {
    check(
      Test.testSuccess(
        XorSection.xorStyleParse _,
        false :: true :: HNil
      )
    )
  }

  def `Xor composes nicely` = {
    check(
      Test.testSuccess(
        XorSection.xorComposition _,
        false :: true :: false :: HNil
      )
    )
  }

  def `Xor can carry exceptions on the left` = {
    check(
      Test.testSuccess(
        XorSection.xorExceptions _,
        "Got reciprocal: 0.5" :: HNil
      )
    )
  }

  def `Xor can carry a value of an error ADT on the left` = {
    check(
      Test.testSuccess(
        XorSection.xorErrorsAsAdts _,
        "Got reciprocal: 0.5" :: HNil
      )
    )
  }

  def `Xor in the large` = {
    check(
      Test.testSuccess(
        XorSection.xorInTheLarge _,
        Xor.right[String, Int](42) :: Xor.left[String, Int]("Hello") :: Xor.left[String, Int]("olleH") :: HNil
      )
    )
  }

  def `Xor with exceptions` = {
    check(
      Test.testSuccess(
        XorSection.xorWithExceptions _,
        false :: true :: HNil
      )
    )
  }

  def `Xor syntax` = {
    check(
      Test.testSuccess(
        XorSection.xorSyntax _,
        Xor.right[String, Int](42) :: HNil
      )
    )
  }
}
