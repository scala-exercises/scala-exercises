package exercises

import catslib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class FoldableSpec extends Spec with Checkers {
  def `foldLeft function` = {
    check(
      Test.testSuccess(
        FoldableSection.foldableFoldLeft _,
        6 :: "abc" :: HNil
      )
    )
  }

  def `foldRight function` = {
    check(
      Test.testSuccess(
        FoldableSection.foldableFoldRight _,
        6 :: HNil
      )
    )
  }

  def `fold function` = {
    check(
      Test.testSuccess(
        FoldableSection.foldableFold _,
        "abc" :: 6 :: HNil
      )
    )
  }

  def `foldMap function` = {
    check(
      Test.testSuccess(
        FoldableSection.foldableFoldMap _,
        3 :: "123" :: HNil
      )
    )
  }

  def `find function` = {
    val aNone: Option[Int] = None

    check(
      Test.testSuccess(
        FoldableSection.foldableFind _,
        Option(3) :: aNone :: HNil
      )
    )
  }

  def `exists function` = {
    check(
      Test.testSuccess(
        FoldableSection.foldableExists _,
        true :: false :: HNil
      )
    )
  }

  def `forall function` = {
    check(
      Test.testSuccess(
        FoldableSection.foldableForall _,
        true :: false :: HNil
      )
    )
  }

  def `foldK function` = {
    val aNone: Option[Unit] = None

    check(
      Test.testSuccess(
        FoldableSection.foldableFoldk _,
        List(1, 2, 3, 4, 5) :: Option("two") :: HNil
      )
    )
  }

  def `tolist function` = {
    val emptyList: List[Int] = Nil

    check(
      Test.testSuccess(
        FoldableSection.foldableTolist _,
        List(1, 2, 3) :: List(42) :: emptyList :: HNil
      )
    )
  }

  def `filter function` = {
    val emptyList: List[Int] = Nil

    check(
      Test.testSuccess(
        FoldableSection.foldableFilter _,
        List(1, 2) :: emptyList :: HNil
      )
    )
  }

  def `traverse_ function` = {
    val aNone: Option[Unit] = None

    check(
      Test.testSuccess(
        FoldableSection.foldableTraverse _,
        Option(()) :: aNone :: HNil
      )
    )
  }

  def `compose foldables` = {
    check(
      Test.testSuccess(
        FoldableSection.foldableCompose _,
        10 :: "123" :: HNil
      )
    )
  }

  def `foldable methods` = {
    check(
      Test.testSuccess(
        FoldableSection.foldableMethods _,
        false :: List(2, 3) :: List(1) :: HNil
      )
    )
  }
}
