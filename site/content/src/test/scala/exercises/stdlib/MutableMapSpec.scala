package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class MutableMapsSpec extends Spec with Checkers {
  def `creation` = {
    check(
      Test.testSuccess(
        MutableMaps.easilyCreatedMutableMaps _,
        4 :: true :: HNil
      )
    )
  }

  def `removal` = {
    check(
      Test.testSuccess(
        MutableMaps.removeElementMutableMaps _,
        false :: HNil
      )
    )
  }

  def `tuple removal` = {
    check(
      Test.testSuccess(
        MutableMaps.removeWithTuplesMutableMaps _,
        false :: 2 :: HNil
      )
    )
  }

  def `tuple addition` = {
    check(
      Test.testSuccess(
        MutableMaps.addWithTuplesMutableMaps _,
        true :: 4 :: HNil
      )
    )
  }

  def `list addition` = {
    check(
      Test.testSuccess(
        MutableMaps.addedElementsMutableMaps _,
        true :: 4 :: HNil
      )
    )
  }

  def `list removal` = {
    check(
      Test.testSuccess(
        MutableMaps.removedElementsMutableMaps _,
        false :: 2 :: HNil
      )
    )
  }

  def `clear` = {
    check(
      Test.testSuccess(
        MutableMaps.clearMapMutableMaps _,
        false :: 0 :: HNil
      )
    )
  }
}
