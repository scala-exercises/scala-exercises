package exercises

import catslib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class MonadSpec extends Spec with Checkers {
  def `flatten function` = {
    val aNone: Option[Int] = None

    check(
      Test.testSuccess(
        MonadSection.flattenRecap _,
        Option(1) :: aNone :: List(1, 2, 3) :: HNil
      )
    )
  }

  def `monad instances` = {
    check(
      Test.testSuccess(
        MonadSection.monadInstances _,
        Option(42) :: HNil
      )
    )
  }

  def `flatmap function` = {
    check(
      Test.testSuccess(
        MonadSection.monadFlatmap _,
        List(1, 1, 2, 2, 3, 3) :: HNil
      )
    )
  }

  def `ifM function` = {
    check(
      Test.testSuccess(
        MonadSection.monadIfm _,
        Option("truthy") :: List(1, 2, 3, 4, 1, 2) :: HNil
      )
    )
  }

  def `monad composition` = {
    check(
      Test.testSuccess(
        MonadSection.monadComposition _,
        List(Option(42)) :: HNil
      )
    )
  }
}
