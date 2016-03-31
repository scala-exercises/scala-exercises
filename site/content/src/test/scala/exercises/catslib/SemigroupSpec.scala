package exercises

import catslib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class SemigroupSpec extends Spec with Checkers {
  def `has a combine operation` = {
    check(
      Test.testSuccess(
        SemigroupSection.semigroupCombine _,
        3 :: List(1, 2, 3, 4, 5, 6) :: Option(3) :: Option(1) :: 67 :: HNil
      )
    )
  }

  def `composes with other semigroups` = {
    check(
      Test.testSuccess(
        SemigroupSection.composingSemigroups _,
        Option(Map("bar" → 11)) :: HNil
      )
    )
  }

  def `has special syntax` = {
    val aNone: Option[Int] = None

    check(
      Test.testSuccess(
        SemigroupSection.semigroupSpecialSyntax _,
        Option(3) :: Option(2) :: aNone :: Option(2) :: HNil
      )
    )
  }
}
