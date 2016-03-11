package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class TuplesSpec extends Spec with Checkers {
  def `are indexed` = {
    check(
      Test.success(
        Tuples.oneIndexedTuples _,
        "apple" :: "dog" :: HNil
      )
    )
  }

  def `are heterogeneous` = {
    check(
      Test.success(
        Tuples.mixedTypeTuples _,
        1 :: "five" :: HNil
      )
    )
  }

  def `support multiple assignment` = {
    check(
      Test.success(
        Tuples.assignVariablesTuples _,
        "Sean Rogers" :: 21 :: 3.5F :: HNil
      )
    )
  }

  def `two tuple elements can be swapped` = {
    check(
      Test.success(
        Tuples.swappedTuples _,
        3 :: "apple" :: HNil
      )
    )
  }
}
