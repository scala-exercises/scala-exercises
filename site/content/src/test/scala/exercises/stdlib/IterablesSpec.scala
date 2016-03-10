package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class IterablesSpec extends Spec with Checkers {
  def `collection iterables` = {
    check(
      Test.success(
        Iterables.collectionIterablesIterables _,
        3 :: HNil
      )
    )
  }

  def `grouped iterables` = {
    check(
      Test.success(
        Iterables.groupedIterables _,
        3 :: 5 :: 9 :: 11 :: 15 :: 19 :: 21 :: 24 :: 32 :: HNil
      )
    )
  }

  def `sliding iterables` = {
    check(
      Test.success(
        Iterables.slidingIterables _,
        3 :: 5 :: 9 :: 5 :: 9 :: 11 :: 9 :: 11 :: 15 :: HNil
      )
    )
  }

  def `sliding window iterables` = {
    check(
      Test.success(
        Iterables.slidingWindowIterables _,
        3 :: 5 :: 9 :: 11 :: 15 :: 19 :: 21 :: 24 :: 32 :: HNil
      )
    )
  }

  def `take right on iterables` = {
    check(
      Test.success(
        Iterables.takeRightIterables _,
        21 :: 24 :: 32 :: HNil
      )
    )
  }

  def `drop right on iterables` = {
    check(
      Test.success(
        Iterables.dropRightIterables _,
        3 :: 5 :: 9 :: 11 :: 15 :: 19 :: HNil
      )
    )
  }

  def `zipping iterables` = {
    check(
      Test.success(
        Iterables.zipIterables _,
        3 :: "Bob" :: 5 :: "Ann" :: 9 :: "Stella" :: HNil
      )
    )
  }

  def `zipping different size iterables` = {
    check(
      Test.success(
        Iterables.sameSizeZipIterables _,
        3 :: "Bob" :: 5 :: "Ann" :: HNil
      )
    )
  }

  def `zipping with zipAll` = {
    check(
      Test.success(
        Iterables.zipAllIterables _,
        3 :: "Bob" :: 5 :: "Ann" :: 9 :: 3 :: "Bob" :: 5 :: "Ann" :: "Stella" :: HNil
      )
    )
  }

  def `zipping with index` = {
    check(
      Test.success(
        Iterables.zipWithIndexIterables _,
        "Manny" :: "Moe" :: 1 :: "Jack" :: HNil
      )
    )
  }

  def `zipping with sameElements` = {
    check(
      Test.success(
        Iterables.sameElementsIterables _,
        true :: false :: true :: false :: HNil
      )
    )
  }
}
