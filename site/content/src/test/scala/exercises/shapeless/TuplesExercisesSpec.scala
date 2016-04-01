package exercises

import shapelessex._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

import shapeless.syntax.std.tuple._

class TuplesExercisesSpec extends Spec with Checkers {

  def `head op` = {
    check(
      Test.testSuccess(
        TuplesExercises.head _,
        23 :: HNil
      )
    )
  }

  def `tail op` = {
    check(
      Test.testSuccess(
        TuplesExercises.tail _,
        ("foo", true) :: HNil
      )
    )
  }

  def `drop op` = {
    check(
      Test.testSuccess(
        TuplesExercises.drop _,
        Tuple1(true) :: HNil
      )
    )
  }

  def `take op` = {
    check(
      Test.testSuccess(
        TuplesExercises.take _,
        (23, "foo") :: HNil
      )
    )
  }

  /* TODO could not find implicit value for parameter fntop: shapeless.ops.function.FnToProduct.Aux[((Int, (String, Boolean))) => Unit,shapeless.::[((Int,), (String, Boolean)),shapeless.HNil] => R]

  def `split op` = {
    check(
      Test.testSuccess(
        TuplesExercises.split _,
        (23, "foo", true).split(1) :: HNil
      )
    )
   }*/

  def `prepend op` = {
    check(
      Test.testSuccess(
        TuplesExercises.prepend _,
        (23, "foo", true) :: HNil
      )
    )
  }

  def `append op` = {
    check(
      Test.testSuccess(
        TuplesExercises.append _,
        (23, "foo", true) :: HNil
      )
    )
  }

  def `concatenate op` = {
    check(
      Test.testSuccess(
        TuplesExercises.concatenate _,
        (23, "foo", true, 2.0) :: HNil
      )
    )
  }

  def `map op` = {
    check(
      Test.testSuccess(
        TuplesExercises.map _,
        (Option(23), Option("foo"), Option(true)) :: HNil
      )
    )
  }

  def `flatMap op` = {
    check(
      Test.testSuccess(
        TuplesExercises.flatMap _,
        (23, "foo", true, 2.0) :: HNil
      )
    )
  }

  def `fold op` = {
    check(
      Test.testSuccess(
        TuplesExercises.fold _,
        11 :: HNil
      )
    )
  }

  def `toHList op` = {
    check(
      Test.testSuccess(
        TuplesExercises.toHList _,
        { 23 :: "foo" :: true :: HNil } :: HNil
      )
    )
  }

}

