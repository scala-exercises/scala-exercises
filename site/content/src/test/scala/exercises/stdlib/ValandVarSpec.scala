package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class ValandVarSpec extends Spec with Checkers {
  def `mutable or immutable` = {
    check(
      Test.testSuccess(
        ValandVar.mutableOrImmutableValandVar _,
        5 :: 7 :: HNil
      )
    )
  }

  def `val is immutable` = {
    check(
      Test.testSuccess(
        ValandVar.valIsInmutableValandVar _,
        5 :: HNil
      )
    )
  }
}
