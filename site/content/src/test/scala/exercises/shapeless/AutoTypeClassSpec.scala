package exercises

import shapelessex._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class AutoTypeClassSpec extends Spec with Checkers {
  def `monoid derivation` = {
    check(
      Test.testSuccess(
        AutoTypeClassExercises.monoidDerivation _,
        36 :: "foobar" :: true :: "foobar" :: 4.0 :: HNil
      )
    )
  }
}
