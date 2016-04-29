package exercises

import shapelessex._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class LensesExercisesSpec extends Spec with Checkers {

  def `get a field` = {
    check(
      Test.testSuccess(
        LensesExercises.get _,
        37 :: HNil
      )
    )
  }

  def `set a field` = {
    check(
      Test.testSuccess(
        LensesExercises.set _,
        38 :: HNil
      )
    )
  }

  def `modify a field` = {
    check(
      Test.testSuccess(
        LensesExercises.modify _,
        38 :: HNil
      )
    )
  }

  def `read a nested field` = {
    check(
      Test.testSuccess(
        LensesExercises.readNested _,
        "Southover Street" :: HNil
      )
    )
  }

  def `update a nested field` = {
    check(
      Test.testSuccess(
        LensesExercises.updateNested _,
        "Montpelier Road" :: HNil
      )
    )
  }

}
