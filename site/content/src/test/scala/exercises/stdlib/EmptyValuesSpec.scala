package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class EmptyValuesSpec extends Spec with Checkers {
  def `empty values` = {
    check(
      Test.testSuccess(
        EmptyValues.emptyValuesEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none is equal to none` = {
    check(
      Test.testSuccess(
        EmptyValues.avoidingNullEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none is identical to none` = {
    check(
      Test.testSuccess(
        EmptyValues.identicalNoneEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none to string` = {
    check(
      Test.testSuccess(
        EmptyValues.noneToStringEmptyValues _,
        "None" :: HNil
      )
    )
  }

  def `none to list` = {
    check(
      Test.testSuccess(
        EmptyValues.noneToListEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none is empty` = {
    check(
      Test.testSuccess(
        EmptyValues.noneToListEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none can be casted` = {
    check(
      Test.testSuccess(
        EmptyValues.noneToAnyEmptyValues _,
        true :: true :: true :: HNil
      )
    )
  }

  def `none is an option` = {
    val theOption: Option[String] = None

    check(
      Test.testSuccess(
        EmptyValues.noneWithOptionEmptyValues _,
        true :: theOption :: HNil
      )
    )
  }

  def `some vs none` = {
    check(
      Test.testSuccess(
        EmptyValues.someAgainstNoneEmptyValues _,
        false :: false :: HNil
      )
    )
  }

  def `getOrElse on none` = {
    check(
      Test.testSuccess(
        EmptyValues.getOrElseEmptyValues _,
        "Some Value" :: "No Value" :: HNil
      )
    )
  }
}
