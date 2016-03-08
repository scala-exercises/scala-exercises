package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

class AssertsSpec extends Spec with Checkers {
  // FIXME: get rid of this
  import org.scalacheck.Shapeless._

  def `scalatest asserts` = {
    check(Test.success(Asserts.scalaTestAsserts _, true :: HNil))
  }

  // FIXME: depends on #259
  // check(success(Asserts.booleansAsserts _, HNil))

  def `values asserts` = {
    check(Test.success(Asserts.valuesAsserts _, 2 :: HNil))
  }
}

class ByNameParameterSpec extends Spec with Checkers {
  // FIXME: get rid of this
  import org.scalacheck.Shapeless._

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
