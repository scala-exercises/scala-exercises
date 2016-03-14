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
      Test.testSuccess(
        Lists.similarToArraysLists _,
        false :: HNil
      )
    )
  }

  def `can be compared for equality` = {
    check(
      Test.testSuccess(
        Lists.sameContentLists _,
        true :: HNil
      )
    )
  }

  def `empty lists are the same regardless of the types they hold` = {
    check(
      Test.testSuccess(
        Lists.nilListsLists _,
        true :: true :: true :: true :: true :: true :: HNil
      )
    )
  }

  def `lists are easily created` = {
    check(
      Test.testSuccess(
        Lists.easilyCreatedLists _,
        1 :: 2 :: 3 :: HNil
      )
    )
  }

  def `head and tail` = {
    check(
      Test.testSuccess(
        Lists.headAndTailLists _,
        1 :: 2 :: 3 :: HNil
      )
    )
  }

  def `access by position` = {
    check(
      Test.testSuccess(
        Lists.byPositionLists _,
        1 :: 5 :: 9 :: HNil
      )
    )
  }

  def `lists are immutable` = {
    check(
      Test.testSuccess(
        Lists.areImmutableLists _,
        1 :: 3 :: 7 :: 9 :: HNil
      )
    )
  }

  def `useful list methods` = {
    check(
      Test.testSuccess(
        Lists.usefulMethodsLists _,
        5 :: List(9, 7, 5, 3, 1) :: List(2, 6, 10, 14, 18) :: List(3, 9) :: HNil
      )
    )
  }

  def `wildcard for anonymous functions` = {
    check(
      Test.testSuccess(
        Lists.wildcardAsShorthandLists _,
        2 :: 4 :: 6 :: 2 :: HNil
      )
    )
  }

  def `functions over lists` = {
    check(
      Test.testSuccess(
        Lists.functionsOverListsLists _,
        2 :: 4 :: 6 :: 1 :: 3 :: HNil
      )
    )
  }

  def `reducing lists` = {
    check(
      Test.testSuccess(
        Lists.reducingListsLists _,
        16 :: 105 :: HNil
      )
    )
  }

  def `foldLeft on lists` = {
    check(
      Test.testSuccess(
        Lists.foldLeftLists _,
        16 :: 26 :: 105 :: 0 :: HNil
      )
    )
  }

  def `lists from range` = {
    check(
      Test.testSuccess(
        Lists.fromRangeLists _,
        List(1, 2, 3, 4, 5) :: HNil
      )
    )
  }

  // FIXME: depends on #259
  // def `lists share tails` = {
  //   check(
  //     Test.testSuccess(
  //       Lists.reuseTailsLists _,
  //       HNil
  //     )
  //   )
  // }
}
