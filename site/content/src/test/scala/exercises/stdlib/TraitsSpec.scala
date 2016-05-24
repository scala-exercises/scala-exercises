package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class TraitsSpec extends Spec with Checkers {
  def `are similar to interfaces` = {
    check(
      Test.testSuccess(
        Traits.similarToInterfacesTraits _,
        "An unfortunate moose stampede occurred" :: HNil
      )
    )
  }

  def `classes can only extend one class or trait` = {
    check(
      Test.testSuccess(
        Traits.extendsFromOneTraits _,
        "An unfortunate woodchuck stampede occurred" :: HNil
      )
    )
  }

  def `are polymorphic` = {
    check(
      Test.testSuccess(
        Traits.polymorphicTraits _,
        true :: true :: true :: true :: HNil
      )
    )
  }

}
