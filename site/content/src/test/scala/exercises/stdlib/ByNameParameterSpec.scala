package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class ByNameParameterSpec extends Spec with Checkers {
  def `takes unit by name parameter` = {
    val right: Either[Throwable, Int] = Right(29)

    check(
      Test.testSuccess(
        ByNameParameter.takesUnitByNameParameter _,
        right :: HNil
      )
    )
  }

  def `takes parameters by name` = {
    val right: Either[Throwable, Int] = Right(69)

    check(
      Test.testSuccess(
        ByNameParameter.byNameParameter _,
        right :: HNil
      )
    )
  }

  def `apply parameters` = {
    check(
      Test.testSuccess(
        ByNameParameter.withApplyByNameParameter _,
        "retzelpay" :: HNil
      )
    )
  }
}
