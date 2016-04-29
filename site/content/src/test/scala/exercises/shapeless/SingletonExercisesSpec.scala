package exercises

import shapelessex._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._
import shapeless._, syntax.singleton._

class SingletonExercisesSpec extends Spec with Checkers {

  def `index into HList and Tuples` = {
    check(
      Test.testSuccess(
        SingletonExercises.indexHListAndTuples _,
        "foo" :: "foo" :: HNil
      )
    )
  }

  def `witness select` = {
    check(
      Test.testSuccess(
        SingletonExercises.select _,
        23 :: "foo" :: HNil
      )
    )
  }

  // TODO disabled until divergent implicit expansion is fixed for singleton types on scalacheck-shapeless int lib 
  //   https://gitter.im/milessabin/shapeless?at=56fcf50fbbffcc665faad6e5
  /*  def `narrow 1` = {
    check(
      Test.testSuccess(
        SingletonExercises.narrow1 _,
        23.narrow :: HNil
      )
    )
  }*/

}
