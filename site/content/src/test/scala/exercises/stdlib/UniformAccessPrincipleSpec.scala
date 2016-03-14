package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class UniformAccessPrincipleSpec extends Spec with Checkers {
  def `uniform access principle` = {
    check(
      Test.success(
        UniformAccessPrinciple.uniformAccessPrincipleUniformAccessPrinciple _,
        7 :: HNil
      )
    )
  }

  def `access as a property` = {
    check(
      Test.success(
        UniformAccessPrinciple.asPropertyUniformAccessPrinciple _,
        7 :: HNil
      )
    )
  }

  def `updating a property` = {
    check(
      Test.success(
        UniformAccessPrinciple.updatingPropertyUniformAccessPrinciple _,
        7 :: HNil
      )
    )
  }

  def `updating using a method` = {
    check(
      Test.success(
        UniformAccessPrinciple.updateUsingMethodUniformAccessPrinciple _,
        8 :: HNil
      )
    )
  }
}
