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
      Test.success(
        MutableMaps.easilyCreatedMutableMaps _,
        4 :: true :: HNil
      )
    )
  }

  def `removal` = {
    check(
      Test.success(
        MutableMaps.removeElementMutableMaps _,
        false :: HNil
      )
    )
  }

  def `tuple removal` = {
    check(
      Test.success(
        MutableMaps.removeWithTuplesMutableMaps _,
        false :: 2 :: HNil
      )
    )
  }

  def `tuple addition` = {
    check(
      Test.success(
        MutableMaps.addWithTuplesMutableMaps _,
        true :: 4 :: HNil
      )
    )
  }

  def `list addition` = {
    check(
      Test.success(
        MutableMaps.addedElementsMutableMaps _,
        true :: 4 :: HNil
      )
    )
  }

  def `list removal` = {
    check(
      Test.success(
        MutableMaps.removedElementsMutableMaps _,
        false :: 2 :: HNil
      )
    )
  }

  def `clear` = {
    check(
      Test.success(
        MutableMaps.clearMapMutableMaps _,
        false :: 0 :: HNil
      )
    )
  }
}
