package exercises

import shapelessex._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class GenericExercisesSpec extends Spec with Checkers {
  def `Generic from to` = {
    check(
      Test.testSuccess(
        GenericExercises.genericE _,
        { 23 :: "foo" :: true :: HNil } :: 13 :: HNil
      )
    )
  }

  def `structural recursive transformation` = {
    check(
      Test.testSuccess(
        GenericExercises.structural _,
        2 :: 3 :: 4 :: HNil
      )
    )
  }

  def `labelled generics` = {
    check(
      Test.testSuccess(
        GenericExercises.labelled _,
        44.11 :: 46.11 :: true :: HNil
      )
    )
  }
}
