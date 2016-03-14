package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class FormattingSpec extends Spec with Checkers {
  def `strings` = {
    check(
      Test.testSuccess(
        Formatting.placedInFormatFormatting _,
        "Application Hello World" :: HNil
      )
    )
  }

  def `characters` = {
    check(
      Test.testSuccess(
        Formatting.characterFormatting _,
        "a" :: "B" :: HNil
      )
    )
  }

  def `escape sequences` = {
    check(
      Test.testSuccess(
        Formatting.escapeSequenceFormatting _,
        "a" :: "a" :: "\"" :: "\\" :: HNil
      )
    )
  }

  def `formatting numbers` = {
    check(
      Test.testSuccess(
        Formatting.includingNumbersFormatting _,
        "90 bottles of beer on the wall" :: HNil
      )
    )
  }

  def `formatting variable numbers of items` = {
    check(
      Test.testSuccess(
        Formatting.anyNumberOfItemsFormatting _,
        "90 bottles of vodka on the wall" :: HNil
      )
    )
  }
}
