package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class OptionsSpec extends Spec with Checkers {
  def `none and some` = {
    val theNone: Option[String] = None

    check(
      Test.success(
        Options.conceptOptions _,
        "I am wrapped in something" :: theNone :: HNil
      )
    )
  }

  def `get method is dangerous` = {
    check(
      Test.success(
        Options.maybeItWillReturnSomethingOptions _,
        "Found value" :: HNil
      )
    )
  }

  def `getOrElse method` = {
    check(
      Test.success(
        Options.getOrElseOptions _,
        "Found value" :: "No value" :: "default function" :: HNil
      )
    )
  }

  def `isEmpty method` = {
    check(
      Test.success(
        Options.isEmptyOptions _,
        false :: true :: HNil
      )
    )
  }

  def `pattern matching` = {
    check(
      Test.success(
        Options.matchOptions _,
        20F :: 0F :: HNil
      )
    )
  }

  def `mapping` = {
    val theNone: Option[Double] = None

    check(
      Test.success(
        Options.mapOptions _,
        Option(4.5D) :: theNone :: HNil
      )
    )
  }

  def `folding` = {
    check(
      Test.success(
        Options.foldOptions _,
        9 :: 0 :: HNil
      )
    )
  }
}
