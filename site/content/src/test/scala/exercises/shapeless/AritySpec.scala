package exercises

import shapelessex._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class AritySpec extends Spec with Checkers {
  def `abstracting over arity` = {
    check(
      Test.testSuccess(
        ArityExercises.arityTest _,
        3 :: 6 :: HNil
      )
    )
  }
}
