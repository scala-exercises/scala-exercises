package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class ByNameParameterSpec extends Spec with Checkers {
  def `takes unit by name parameter` = {
    val right: Either[Throwable, Int] = Right(29)

    check(
      Test.success(
        ByNameParameter.takesUnitByNameParameter _,
        right :: HNil
      )
    )
  }

  def `takes parameters by name` = {
    val right: Either[Throwable, Int] = Right(69)

    check(
      Test.success(
        ByNameParameter.byNameParameter _,
        right :: HNil
      )
    )
  }

  def `apply parameters` = {
    check(
      Test.success(
        ByNameParameter.withApplyByNameParameter _,
        "retzelpay" :: HNil
      )
    )
  }
}
