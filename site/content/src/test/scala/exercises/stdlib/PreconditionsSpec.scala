package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class PreconditionsSpec extends Spec with Checkers {
  def `can be added to constructors` = {
    check(
      Test.testSuccess(
        Preconditions.preconditionsPreconditions _,
        27 :: HNil
      )
    )
  }
}
