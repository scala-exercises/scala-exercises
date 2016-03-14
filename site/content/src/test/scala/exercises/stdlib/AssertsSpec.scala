package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class AssertsSpec extends Spec with Checkers {
  def `scalatest asserts` = {
    check(Test.testSuccess(Asserts.scalaTestAsserts _, true :: HNil))
  }

  // FIXME: depends on #259
  // check(testSuccess(Asserts.booleansAsserts _, HNil))

  def `values asserts` = {
    check(Test.testSuccess(Asserts.valuesAsserts _, 2 :: HNil))
  }
}
