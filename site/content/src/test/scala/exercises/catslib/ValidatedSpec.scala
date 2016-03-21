package exercises

import catslib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class ValidatedSpec extends Spec with Checkers {
  def `with no errors` = {
    check(
      Test.testSuccess(
        ValidatedSection.noErrors _,
        true :: "127.0.0.1" :: 1337 :: HNil
      )
    )
  }

  def `with accumulating errors` = {
    check(
      Test.testSuccess(
        ValidatedSection.someErrors _,
        false :: true :: HNil
      )
    )
  }

  def `sequential validation` = {
    check(
      Test.testSuccess(
        ValidatedSection.sequentialValidation _,
        false :: true :: HNil
      )
    )
  }

  def `validation with xor` = {
    check(
      Test.testSuccess(
        ValidatedSection.validatedAsXor _,
        false :: true :: HNil
      )
    )
  }
}
