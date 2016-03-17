package exercises

import catslib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class IdentitySpec extends Spec with Checkers {
  def `Id type` = {
    check(
      Test.testSuccess(
        IdentitySection.identityType _,
        42 :: HNil
      )
    )
  }

  def `Id has pure` = {
    check(
      Test.testSuccess(
        IdentitySection.pureIdentity _,
        42 :: HNil
      )
    )
  }

  def `Id Comonad` = {
    check(
      Test.testSuccess(
        IdentitySection.idComonad _,
        43 :: HNil
      )
    )
  }
}
