package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class ExtractorsSpec extends Spec with Checkers {
  def `extractors` = {
    check(
      Test.testSuccess(
        Extractors.extractorsExtractors _,
        "Where's Batman?" :: HNil
      )
    )
  }

  def `unapply` = {
    check(
      Test.testSuccess(
        Extractors.calledUnapplyExtractors _,
        "Chevy" :: "Camaro" :: 1978 :: 120 :: HNil
      )
    )
  }

  def `pattern matching` = {
    check(
      Test.testSuccess(
        Extractors.patternMatchingExtractors _,
        "Chevy" :: "Camaro" :: HNil
      )
    )
  }

  def `wildcards` = {
    check(
      Test.testSuccess(
        Extractors.withWildcardExtractors _,
        "Chevy" :: "Camaro" :: HNil
      )
    )
  }

  def `multiple extractors` = {
    check(
      Test.testSuccess(
        Extractors.multipleUnapplyExtractors _,
        "c: Kurt, d: Vonnegut" :: HNil
      )
    )
  }

  def `any object as extractor` = {
    check(
      Test.testSuccess(
        Extractors.anyObjectExtractors _,
        "make: Chevy, model: Camaro" :: HNil
      )
    )
  }

  def `extractor in companion object` = {
    val theOption: Option[String] = None

    check(
      Test.testSuccess(
        Extractors.asAssignmentExtractors _,
        "Keerthi" :: theOption :: "Singri" :: HNil
      )
    )
  }

  def `unapply for pattern matching extractors` = {
    check(
      Test.testSuccess(
        Extractors.unapplyForPatternMatchingExtractors _,
        "I don't care, going on break" :: HNil
      )
    )
  }
}
