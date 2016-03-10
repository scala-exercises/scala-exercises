package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class MapsSpec extends Spec with Checkers {
  def `size` = {
    check(
      Test.success(
        Maps.keyAndValueMaps _,
        4 :: HNil
      )
    )
  }

  def `repeated keys` = {
    check(
      Test.success(
        Maps.distinctPairingsMaps _,
        3 :: HNil
      )
    )
  }

  def `adding to maps` = {
    check(
      Test.success(
        Maps.easilyAddedMaps _,
        true :: HNil
      )
    )
  }

  def `iterating over maps` = {
    check(
      Test.success(
        Maps.canBeIteratedMaps _,
        3 :: "Michigan" :: "Wisconsin" :: HNil
      )
    )
  }

  def `duplicate keys` = {
    check(
      Test.success(
        Maps.duplicatedKeyInsertionMaps _,
        3 :: "Meechigan" :: HNil
      )
    )
  }

  def `mixed key types` = {
    check(
      Test.success(
        Maps.mixedTypeKeysMaps _,
        "MI" :: "MI" :: HNil
      )
    )
  }

  def `mixed value types` = {
    check(
      Test.success(
        Maps.mixedTypeValuesMaps _,
        49931 :: 48103 :: 48104 :: 48108 :: HNil
      )
    )
  }

  def `map key access` = {
    check(
      Test.success(
        Maps.mayBeAccessedMaps _,
        "Michigan" :: "Iowa" :: HNil
      )
    )
  }

  def `map element removal` = {
    check(
      Test.success(
        Maps.easilyRemovedMaps _,
        false :: true :: HNil
      )
    )
  }

  def `not found keys` = {
    check(
      Test.success(
        Maps.keyNotFoundMaps _,
        true :: HNil
      )
    )
  }

  def `multiple key removal` = {
    check(
      Test.success(
        Maps.removedInMultipleMaps _,
        false :: true :: true :: 2 :: 4 :: HNil
      )
    )
  }

  def `key removal with tuples` = {
    check(
      Test.success(
        Maps.removedWithTupleMaps _,
        false :: true :: true :: 2 :: 4 :: HNil
      )
    )
  }

  def `non-existent element removal` = {
    check(
      Test.success(
        Maps.attemptedRemovalMaps _,
        true :: HNil
      )
    )
  }

  def `maps dont have ordering` = {
    check(
      Test.success(
        Maps.orderIndependentMaps _,
        true :: HNil
      )
    )
  }
}
