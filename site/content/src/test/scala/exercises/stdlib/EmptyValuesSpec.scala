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
      Test.success(
        EmptyValues.emptyValuesEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none is equal to none` = {
    check(
      Test.success(
        EmptyValues.avoidingNullEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none is identical to none` = {
    check(
      Test.success(
        EmptyValues.identicalNoneEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none to string` = {
    check(
      Test.success(
        EmptyValues.noneToStringEmptyValues _,
        "None" :: HNil
      )
    )
  }

  def `none to list` = {
    check(
      Test.success(
        EmptyValues.noneToListEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none is empty` = {
    check(
      Test.success(
        EmptyValues.noneToListEmptyValues _,
        true :: HNil
      )
    )
  }

  def `none can be casted` = {
    check(
      Test.success(
        EmptyValues.noneToAnyEmptyValues _,
        true :: true :: true :: HNil
      )
    )
  }

  def `none is an option` = {
    val theOption: Option[String] = None

    check(
      Test.success(
        EmptyValues.noneWithOptionEmptyValues _,
        true :: theOption :: HNil
      )
    )
  }

  def `some vs none` = {
    check(
      Test.success(
        EmptyValues.someAgainstNoneEmptyValues _,
        false :: false :: HNil
      )
    )
  }

  def `getOrElse on none` = {
    check(
      Test.success(
        EmptyValues.getOrElseEmptyValues _,
        "Some Value" :: "No Value" :: HNil
      )
    )
  }
}
