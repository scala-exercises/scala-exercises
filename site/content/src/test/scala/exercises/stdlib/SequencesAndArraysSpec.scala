package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class SequencesandArraysSpec extends Spec with Checkers {
  def `list to array` = {
    check(
      Test.success(
        SequencesandArrays.sequentialCollectionSequencesandArrays _,
        1 :: 2 :: 3 :: HNil
      )
    )
  }

  def `seq to list` = {
    check(
      Test.success(
        SequencesandArrays.orderedElementsSequencesandArrays _,
        List(1, 2, 3) :: HNil
      )
    )
  }

  def `sequences with for comprehensions` = {
    check(
      Test.success(
        SequencesandArrays.fromForComprehensionSequencesandArrays _,
        List(1, 2, 3, 4) :: HNil
      )
    )
  }

  def `sequences with for comprehension and conditions` = {
    check(
      Test.success(
        SequencesandArrays.withConditionSequencesandArrays _,
        List(3, 6, 9) :: HNil
      )
    )
  }

  def `filtered sequences` = {
    check(
      Test.success(
        SequencesandArrays.filterPredicateSequencesandArrays _,
        "hello" :: "you" :: HNil
      )
    )
  }

  def `filtered arrays` = {
    check(
      Test.success(
        SequencesandArrays.filterArraySequencesandArrays _,
        "hello" :: "again" :: HNil
      )
    )
  }

  def `map over a sequence` = {
    check(
      Test.success(
        SequencesandArrays.mapValuesSequencesandArrays _,
        List("olleh", "dlrow") :: HNil
      )
    )
  }
}
