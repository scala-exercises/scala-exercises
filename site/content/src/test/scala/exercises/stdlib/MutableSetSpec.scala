package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class MutableSetsSpec extends Spec with Checkers {
  def `creation` = {
    check(
      Test.success(
        MutableSets.easilyCreatedMutableSets _,
        4 :: true :: HNil
      )
    )
  }

  def `removal` = {
    check(
      Test.success(
        MutableSets.removeElementMutableSets _,
        false :: HNil
      )
    )
  }

  def `tuple removal` = {
    check(
      Test.success(
        MutableSets.removeWithTuplesMutableSets _,
        false :: 2 :: HNil
      )
    )
  }

  def `tuple addition` = {
    check(
      Test.success(
        MutableSets.addWithTuplesMutableSets _,
        true :: 4 :: HNil
      )
    )
  }

  def `list addition` = {
    check(
      Test.success(
        MutableSets.addedElementsMutableSets _,
        true :: 4 :: HNil
      )
    )
  }

  def `list removal` = {
    check(
      Test.success(
        MutableSets.removedElementsMutableSets _,
        false :: 2 :: HNil
      )
    )
  }

  def `clear` = {
    check(
      Test.success(
        MutableSets.clearSetMutableSets _,
        false :: 0 :: HNil
      )
    )
  }
}
