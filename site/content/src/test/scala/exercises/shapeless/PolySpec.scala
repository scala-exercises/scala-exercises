package exercises

import shapelessex._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class PolySpec extends Spec with Checkers {
  def `poly choose` = {
    check(
      Test.testSuccess(
        PolyExercises.exerciseChoose _,
        Option(1) :: Option('a') :: HNil
      )
    )
  }

  def `pair apply` = {
    check(
      Test.testSuccess(
        PolyExercises.exercisePairApply _,
        Option(1) :: Option('a') :: HNil
      )
    )
  }

  def `mono choose` = {
    check(
      Test.testSuccess(
        PolyExercises.exerciseMonomorphicChoose _,
        Option(2) :: Option(1) :: HNil
      )
    )
  }

  def `ex size` = {
    check(
      Test.testSuccess(
        PolyExercises.exerciseSize _,
        1 :: 3 :: 4 :: 5 :: HNil
      )
    )
  }

}
