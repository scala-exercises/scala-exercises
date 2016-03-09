package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class AssertsSpec extends Spec with Checkers {
  def `scalatest asserts` = {
    check(Test.success(Asserts.scalaTestAsserts _, true :: HNil))
  }

  // FIXME: depends on #259
  // check(success(Asserts.booleansAsserts _, HNil))

  def `values asserts` = {
    check(Test.success(Asserts.valuesAsserts _, 2 :: HNil))
  }
}
