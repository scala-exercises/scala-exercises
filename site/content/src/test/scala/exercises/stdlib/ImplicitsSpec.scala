package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class ImplicitsSpec extends Spec with Checkers {
  def `implicit parameters` = {
    check(
      Test.testSuccess(
        Implicits.implicitsParametersImplicits _,
        true :: false :: HNil
      )
    )
  }

  def `imported implicits` = {
    check(
      Test.testSuccess(
        Implicits.importedImplicits _,
        true :: false :: HNil
      )
    )
  }

  def `type conversion implicits` = {
    check(
      Test.testSuccess(
        Implicits.convertTypeImplicits _,
        true :: false :: true :: true :: true :: HNil
      )
    )
  }

  def `implicits for default parameters` = {
    val fstAnswer: Float = 1020.0f
    val sndAnswer: Float = 9025.0f

    check(
      Test.testSuccess(
        Implicits.asDefaultImplicits _,
        fstAnswer :: sndAnswer :: HNil
      )
    )
  }

  def `multiple implicits for default parameters` = {
    check(
      Test.testSuccess(
        Implicits.listOfImplicitsImplicits _,
        "1020.0 Dollars" :: "9025.0 Dollars" :: HNil
      )
    )
  }

  def `default arguments instead of implicits` = {
    check(
      Test.testSuccess(
        Implicits.defaultArgumentsImplicits _,
        "1020 Dollars" :: "2850 Dollars" :: HNil
      )
    )
  }
}
