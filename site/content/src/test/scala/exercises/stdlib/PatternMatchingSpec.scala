package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class PatternMatchingSpec extends Spec with Checkers {
  def `pattern matching` = {
    check(
      Test.testSuccess(
        PatternMatching.patternMatchingMechanismPatternMatching _,
        2 :: HNil
      )
    )
  }

  def `pattern matching with complex result` = {
    check(
      Test.testSuccess(
        PatternMatching.returnComplexPatternMatching _,
        0 :: 0 :: 255 :: HNil
      )
    )
  }

  def `pattern matching with complex expressions` = {
    check(
      Test.testSuccess(
        PatternMatching.complexExpressionsPatternMatching _,
        "Mama eating porridge" :: HNil
      )
    )
  }

  def `pattern matching with wildcard parts` = {
    check(
      Test.testSuccess(
        PatternMatching.wildcardParsPatternMatching _,
        "eating" :: "sitting" :: HNil
      )
    )
  }

  def `pattern matching with substitution in parts` = {
    check(
      Test.testSuccess(
        PatternMatching.substitutePartsPatternMatching _,
        "Papa said someone's been eating my porridge" :: "Mama said someone's been sitting in my chair" :: HNil
      )
    )
  }

  def `pattern matching with scoped variables` = {
    check(
      Test.testSuccess(
        PatternMatching.createCaseStatementPatternMatching _,
        "eating" :: "sitting" :: "eating" :: "what?" :: HNil
      )
    )
  }

  def `pattern matching with parameterized variables` = {
    check(
      Test.testSuccess(
        PatternMatching.stableVariablePatternMatching _,
        true :: false :: true :: HNil
      )
    )
  }

  def `pattern matching lists` = {
    check(
      Test.testSuccess(
        PatternMatching.againstListsPatternMatching _,
        2 :: HNil
      )
    )
  }

  def `pattern matching lists part two` = {
    check(
      Test.testSuccess(
        PatternMatching.againstListsIIPatternMatching _,
        2 :: HNil
      )
    )
  }

  def `pattern matching lists part three` = {
    check(
      Test.testSuccess(
        PatternMatching.againstListsIIIPatternMatching _,
        0 :: HNil
      )
    )
  }

  def `pattern matching lists part four` = {
    check(
      Test.testSuccess(
        PatternMatching.againstListsIVPatternMatching _,
        0 :: HNil
      )
    )
  }
}
