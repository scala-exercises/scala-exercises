package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class ListsSpec extends Spec with Checkers {
  def `are homogeneous` = {
    check(
      Test.success(
        Lists.similarToArraysLists _,
        false :: HNil
      )
    )
  }

  def `can be compared for equality` = {
    check(
      Test.success(
        Lists.sameContentLists _,
        true :: HNil
      )
    )
  }

  def `empty lists are the same regardless of the types they hold` = {
    check(
      Test.success(
        Lists.nilListsLists _,
        true :: true :: true :: true :: true :: true :: HNil
      )
    )
  }

  def `lists are easily created` = {
    check(
      Test.success(
        Lists.easilyCreatedLists _,
        1 :: 2 :: 3 :: HNil
      )
    )
  }

  def `head and tail` = {
    check(
      Test.success(
        Lists.headAndTailLists _,
        1 :: 2 :: 3 :: HNil
      )
    )
  }

  def `access by position` = {
    check(
      Test.success(
        Lists.byPositionLists _,
        1 :: 5 :: 9 :: HNil
      )
    )
  }

  def `lists are immutable` = {
    check(
      Test.success(
        Lists.areImmutableLists _,
        1 :: 3 :: 7 :: 9 :: HNil
      )
    )
  }

  def `useful list methods` = {
    check(
      Test.success(
        Lists.usefulMethodsLists _,
        5 :: List(9, 7, 5, 3, 1) :: List(2, 6, 10, 14, 18) :: List(3, 9) :: HNil
      )
    )
  }

  def `wildcard for anonymous functions` = {
    check(
      Test.success(
        Lists.wildcardAsShorthandLists _,
        2 :: 4 :: 6 :: 2 :: HNil
      )
    )
  }

  def `functions over lists` = {
    check(
      Test.success(
        Lists.functionsOverListsLists _,
        2 :: 4 :: 6 :: 1 :: 3 :: HNil
      )
    )
  }

  def `reducing lists` = {
    check(
      Test.success(
        Lists.reducingListsLists _,
        16 :: 105 :: HNil
      )
    )
  }

  def `foldLeft on lists` = {
    check(
      Test.success(
        Lists.foldLeftLists _,
        16 :: 26 :: 105 :: 0 :: HNil
      )
    )
  }

  def `lists from range` = {
    check(
      Test.success(
        Lists.fromRangeLists _,
        List(1, 2, 3, 4, 5) :: HNil
      )
    )
  }

  // FIXME: depends on #259
  // def `lists share tails` = {
  //   check(
  //     Test.success(
  //       Lists.reuseTailsLists _,
  //       HNil
  //     )
  //   )
  // }
}
