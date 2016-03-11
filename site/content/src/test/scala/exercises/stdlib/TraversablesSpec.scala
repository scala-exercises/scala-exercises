package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class TraversablesSpec extends Spec with Checkers {
  def `are at the top of collection hierarchy` = {
    check(
      Test.success(
        Traversables.topOfCollectionTraversables _,
        7 :: 8 :: HNil
      )
    )
  }

  def `can be mapped over` = {
    check(
      Test.success(
        Traversables.mapFunctionTraversables _,
        24 :: HNil
      )
    )
  }

  def `can be flattened` = {
    check(
      Test.success(
        Traversables.flattenFunctionTraversables _,
        List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) :: HNil
      )
    )
  }

  def `can be flatmapped` = {
    check(
      Test.success(
        Traversables.flatMapFunctionTraversables _,
        List(4, 8, 12, 16, 20, 24, 28, 32, 36, 40) :: HNil
      )
    )
  }

  def `can be collected` = {
    check(
      Test.success(
        Traversables.collectFunctionTraversables _,
        List(12, 18, 24, 42) :: HNil
      )
    )
  }

  def `can be collected part two` = {
    check(
      Test.success(
        Traversables.collectFunctionIITraversables _,
        List(12, 18, 28, 24, 36, 52, 42) :: HNil
      )
    )
  }

  def `foreach function` = {
    check(
      Test.success(
        Traversables.foreachFunctionTraversables _,
        List(4, 6, 7, 8, 9, 13, 14) :: HNil
      )
    )
  }

  def `to array function` = {
    check(
      Test.success(
        Traversables.toArrayFunctionTraversables _,
        true :: HNil
      )
    )
  }

  def `to list function` = {
    check(
      Test.success(
        Traversables.toListFunctionTraversables _,
        true :: HNil
      )
    )
  }

  def `to list function part two` = {
    check(
      Test.success(
        Traversables.toListFunctionIITraversables _,
        true :: HNil
      )
    )
  }

  def `to iterable function` = {
    check(
      Test.success(
        Traversables.toIterableFunctionTraversables _,
        true :: HNil
      )
    )
  }

  def `to seq function` = {
    check(
      Test.success(
        Traversables.toSeqFunctionTraversables _,
        true :: HNil
      )
    )
  }

  def `to indexed seq function` = {
    check(
      Test.success(
        Traversables.toIndexedSeqFunctionTraversables _,
        true :: HNil
      )
    )
  }

  def `to stream function` = {
    check(
      Test.success(
        Traversables.toStreamFunctionTraversables _,
        true :: List(4, 6, 7) :: HNil
      )
    )
  }

  def `to set function` = {
    check(
      Test.success(
        Traversables.toSetFunctionTraversables _,
        true :: HNil
      )
    )
  }

  def `to map function` = {
    check(
      Test.success(
        Traversables.toMapFunctionTraversables _,
        true :: HNil
      )
    )
  }

  def `to map function part two` = {
    check(
      Test.success(
        Traversables.toMapFunctionIITraversables _,
        true :: HNil
      )
    )
  }

  def `nonEmpty function` = {
    check(
      Test.success(
        Traversables.nonEmptyFunctionTraversables _,
        true :: false :: HNil
      )
    )
  }

  def `size function` = {
    check(
      Test.success(
        Traversables.sizeFunctionTraversables _,
        2 :: HNil
      )
    )
  }

  def `hasDefiniteSize function` = {
    check(
      Test.success(
        Traversables.hasDefiniteSizeFunctionTraversables _,
        true :: false :: HNil
      )
    )
  }

  def `head function` = {
    check(
      Test.success(
        Traversables.headFunctionTraversables _,
        10 :: HNil
      )
    )
  }

  def `headOption function` = {
    val theNone: Option[Int] = None

    check(
      Test.success(
        Traversables.headOptionFunctionTraversables _,
        Option(10) :: theNone :: HNil
      )
    )
  }

  def `last function` = {
    check(
      Test.success(
        Traversables.lastFunctionTraversables _,
        22 :: HNil
      )
    )
  }

  def `lastOption function` = {
    val theNone: Option[Int] = None

    check(
      Test.success(
        Traversables.lastOptionFunctionTraversables _,
        Option(22) :: theNone :: HNil
      )
    )
  }

  def `find function` = {
    val theNone: Option[Int] = None

    check(
      Test.success(
        Traversables.findFunctionTraversables _,
        Option(19) :: theNone :: HNil
      )
    )
  }

  def `tail function` = {
    check(
      Test.success(
        Traversables.tailFunctionTraversables _,
        List(19, 45, 1, 22) :: HNil
      )
    )
  }

  def `init function` = {
    check(
      Test.success(
        Traversables.initFunctionTraversables _,
        List(10, 19, 45, 1) :: HNil
      )
    )
  }

  def `slice function` = {
    check(
      Test.success(
        Traversables.sliceFunctionTraversables _,
        List(19, 45) :: HNil
      )
    )
  }

  def `take function` = {
    check(
      Test.success(
        Traversables.takeFunctionTraversables _,
        List(10, 19, 45) :: HNil
      )
    )
  }

  def `take function part two` = {
    check(
      Test.success(
        Traversables.takeFunctionIITraversables _,
        List(2, 3, 4) :: HNil
      )
    )
  }

  def `drop function` = {
    check(
      Test.success(
        Traversables.dropFunctionTraversables _,
        List(8, 9, 10) :: HNil
      )
    )
  }

  def `takeWhile function` = {
    check(
      Test.success(
        Traversables.takeWhileFunctionTraversables _,
        List(87, 44, 5, 4) :: HNil
      )
    )
  }

  def `dropWhile function` = {
    check(
      Test.success(
        Traversables.dropWhileFunctionTraversables _,
        List(200, 10, 39, 100) :: HNil
      )
    )
  }

  def `filter function` = {
    check(
      Test.success(
        Traversables.filterFunctionTraversables _,
        Array(87, 44, 5, 4, 10, 39) :: HNil
      )
    )
  }

  def `filterNot function` = {
    check(
      Test.success(
        Traversables.filterNotFunctionTraversables _,
        Array(200, 100) :: HNil
      )
    )
  }

  def `splitAt function` = {
    check(
      Test.success(
        Traversables.splitAtFunctionTraversables _,
        Array(87, 44, 5) :: Array(4, 200, 10, 39, 100) :: HNil
      )
    )
  }

  def `span function` = {
    check(
      Test.success(
        Traversables.spanFunctionTraversables _,
        Array(87, 44, 5, 4) :: Array(200, 10, 39, 100) :: HNil
      )
    )
  }

  def `partition function` = {
    check(
      Test.success(
        Traversables.partitionFunctionTraversables _,
        Array(87, 44, 5, 4, 10, 39) :: Array(200, 100) :: HNil
      )
    )
  }

  def `groupBy function` = {
    check(
      Test.success(
        Traversables.groupByFunctionTraversables _,
        3 :: 2 :: HNil
      )
    )
  }

  def `forall function` = {
    check(
      Test.success(
        Traversables.forallFunctionTraversables _,
        false :: HNil
      )
    )
  }

  def `exists function` = {
    check(
      Test.success(
        Traversables.existsFunctionTraversables _,
        true :: HNil
      )
    )
  }

  def `count function` = {
    check(
      Test.success(
        Traversables.countFunctionTraversables _,
        6 :: HNil
      )
    )
  }

  def `foldLeft function` = {
    check(
      Test.success(
        Traversables.foldLeftFunctionTraversables _,
        -15 :: -15 :: -15 :: -15 :: -15 :: HNil
      )
    )
  }

  def `foldRight function` = {
    check(
      Test.success(
        Traversables.foldRightFunctionTraversables _,
        3 :: 3 :: 3 :: 3 :: 3 :: HNil
      )
    )
  }

  def `reduceLeft function` = {
    check(
      Test.success(
        Traversables.reduceLeftFunctionTraversables _,
        15 :: "DoReMeFaSoLaTeDo" :: HNil
      )
    )
  }

  def `reduceRight function` = {
    check(
      Test.success(
        Traversables.reduceRightFunctionTraversables _,
        15 :: "DoReMeFaSoLaTeDo" :: HNil
      )
    )
  }

  def `sum function and friends` = {
    check(
      Test.success(
        Traversables.sumFunctionTraversables _,
        15 :: 120 :: 5 :: 1 :: HNil
      )
    )
  }

  // FIXME: this test depends on runtime timing of code
  // def `performant traversals` = {
  //   check(
  //     Test.success(
  //       Traversables.performantTraversables _,
  //       false :: HNil
  //     )
  //   )
  // }

  def `transpose function` = {
    check(
      Test.success(
        Traversables.transposeFunctionTraversables _,
        List(1, 4, 7) :: List(2, 5, 8) :: List(3, 6, 9) :: List(1, 4) :: HNil
      )
    )
  }

  def `mkString function` = {
    check(
      Test.success(
        Traversables.mkStringFunctionTraversables _,
        "1,2,3,4,5" :: HNil
      )
    )
  }

  def `mkString function part two` = {
    check(
      Test.success(
        Traversables.mkStringFunctionIITraversables _,
        ">1,2,3,4,5<" :: HNil
      )
    )
  }

  def `addString function` = {
    check(
      Test.success(
        Traversables.addStringFunctionTraversables _,
        "I want all numbers 6-12: 6,7,8,9,10,11,12" :: HNil
      )
    )
  }

  def `views over traversables` = {
    check(
      Test.success(
        Traversables.viewsTraversables _,
        "Doubling 1" :: "Doubling 2" :: "Doubling 3" :: "Adding 1 to 2" :: "Adding 1 to 4" :: "Adding 1 to 6" ::
          "Doubling 1" :: "Adding 1 to 2" :: "Doubling 2" :: "Adding 1 to 4" :: "Doubling 3" :: "Adding 1 to 6" :: HNil
      )
    )
  }

  def `views over traversables can be forced` = {
    check(
      Test.success(
        Traversables.viewForceTraversables _,
        List(60, 70, 80) :: HNil
      )
    )
  }
}
