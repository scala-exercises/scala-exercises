package exercises

import catslib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class FunctorSpec extends Spec with Checkers {
  def `using functor` = {
    val theNone: Option[Int] = None

    check(
      Test.testSuccess(
        FunctorSection.usingFunctor _,
        Option(5) :: theNone :: HNil
      )
    )
  }

  def `lifting to a functor` = {
    check(
      Test.testSuccess(
        FunctorSection.liftingToAFunctor _,
        Option(5) :: HNil
      )
    )
  }

  def `using fproduct` = {
    check(
      Test.testSuccess(
        FunctorSection.usingFproduct _,
        4 :: 2 :: 7 :: HNil
      )
    )
  }

  def `composing functors` = {
    val result: List[Option[Int]] = List(Option(2), None, Option(4))

    check(
      Test.testSuccess(
        FunctorSection.composingFunctors _,
        result :: HNil
      )
    )
  }
}
