package exercises

import shapelessex._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class SizedExercisesSpec extends Spec with Checkers {
  def `Sized usage` = {
    check(
      Test.testSuccess(
        SizedExercises.sizedEx _,
        1 :: HNil
      )
    )
  }
}
