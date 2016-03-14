package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class SetsSpec extends Spec with Checkers {
  def `have no duplicates` = {
    check(
      Test.testSuccess(
        Sets.noDuplicatesSets _,
        4 :: HNil
      )
    )
  }

  def `all their values are distinct` = {
    check(
      Test.testSuccess(
        Sets.distinctValuesSets _,
        3 :: HNil
      )
    )
  }

  def `adding values to sets` = {
    check(
      Test.testSuccess(
        Sets.easilyAddedSets _,
        true :: false :: HNil
      )
    )
  }

  def `sets accept mixed types` = {
    check(
      Test.testSuccess(
        Sets.mixedTypeSets _,
        true :: false :: HNil
      )
    )
  }

  def `we can check for member existence` = {
    check(
      Test.testSuccess(
        Sets.checkExistenceSets _,
        true :: false :: HNil
      )
    )
  }

  def `we can remove members` = {
    check(
      Test.testSuccess(
        Sets.easilyRemovedSets _,
        false :: true :: HNil
      )
    )
  }

  def `we can remove multiple members` = {
    check(
      Test.testSuccess(
        Sets.multipleRemovingSets _,
        false :: true :: 2 :: HNil
      )
    )
  }

  def `we can remove multiple members with tuples` = {
    check(
      Test.testSuccess(
        Sets.tupleRemovingSets _,
        false :: true :: 2 :: HNil
      )
    )
  }

  def `we can remove values that are not part of the set` = {
    check(
      Test.testSuccess(
        Sets.nonexistentRemovalSets _,
        true :: HNil
      )
    )
  }

  def `we can iterate over set values` = {
    check(
      Test.testSuccess(
        Sets.easilyIteratedSets _,
        17 :: HNil
      )
    )
  }

  def `set intersection` = {
    check(
      Test.testSuccess(
        Sets.easilyIntersectedSets _,
        true :: HNil
      )
    )
  }

  def `set union` = {
    check(
      Test.testSuccess(
        Sets.easilyJoinedSets _,
        true :: HNil
      )
    )
  }

  def `subsets` = {
    check(
      Test.testSuccess(
        Sets.subsetSets _,
        false :: true :: HNil
      )
    )
  }

  def `set difference` = {
    check(
      Test.testSuccess(
        Sets.easilyObtainedDifferencesSets _,
        true :: HNil
      )
    )
  }

  def `set equality` = {
    check(
      Test.testSuccess(
        Sets.equivalencySets _,
        true :: HNil
      )
    )
  }
}
