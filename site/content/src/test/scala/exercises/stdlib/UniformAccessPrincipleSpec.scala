package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class UniformAccessPrincipleSpec extends Spec with Checkers {
  def `uniform access principle` = {
    check(
      Test.testSuccess(
        UniformAccessPrinciple.uniformAccessPrincipleUniformAccessPrinciple _,
        10 :: 11 :: HNil
      )
    )
  }

}
