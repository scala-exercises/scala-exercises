package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class LiteralStringsSpec extends Spec with Checkers {
  def `character literals` = {
    check(
      Test.testSuccess(
        LiteralStrings.characterLiteralsLiteralStrings _,
        "a" :: "B" :: HNil
      )
    )
  }

  def `unicode character literals` = {
    check(
      Test.testSuccess(
        LiteralStrings.characterLiteralsUnicodeLiteralStrings _,
        "a" :: HNil
      )
    )
  }

  def `octal character literals` = {
    check(
      Test.testSuccess(
        LiteralStrings.characterLiteralsOctalLiteralStrings _,
        "a" :: HNil
      )
    )
  }

  def `escape sequence character literals` = {
    check(
      Test.testSuccess(
        LiteralStrings.escapeSequenceLiteralStrings _,
        "\"" :: "\\" :: HNil
      )
    )
  }

  def `one line literal strings` = {
    check(
      Test.testSuccess(
        LiteralStrings.oneLineLiteralStrings _,
        "To be or not to be" :: HNil
      )
    )
  }
}
