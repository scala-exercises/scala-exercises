package exercises

import catslib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class ApplicativeSpec extends Spec with Checkers {
  def `pure method` = {
    check(
      Test.testSuccess(
        ApplicativeSection.pureMethod _,
        Option(1) :: List(1) :: HNil
      )
    )
  }

  def `applicative composition` = {
    check(
      Test.testSuccess(
        ApplicativeSection.applicativeComposition _,
        List(Option(1)) :: HNil
      )
    )
  }

  def `applicative and monad` = {
    check(
      Test.testSuccess(
        ApplicativeSection.applicativesAndMonads _,
        Option(1) :: Option(1) :: HNil
      )
    )
  }
}
