package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class ParentClassesSpec extends Spec with Checkers {
  def `all values are objects` = {
    check(
      Test.testSuccess(
        ParentClasses.allValuesAreObjectsParentClasses _,
        "John" :: "Yossarian" :: HNil
      )
    )
  }

  def `subtyping` = {
    check(
      Test.testSuccess(
        ParentClasses.polymorphicParentClasses _,
        "John" :: "Yossarian" :: HNil
      )
    )
  }

  def `abstract classes` = {
    check(
      Test.testSuccess(
        ParentClasses.abstractClassParentClasses _,
        22 :: HNil
      )
    )
  }
}
