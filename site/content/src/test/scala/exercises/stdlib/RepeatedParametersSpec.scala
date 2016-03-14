package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class RepeatedParametersSpec extends Spec with Checkers {
  def `multiple last parameters` = {
    check(
      Test.testSuccess(
        RepeatedParameters.theLastParameterRepeatedParameters _,
        "3 eggs can give you a delicious sandwich, protein, high cholesterol" :: HNil
      )
    )
  }

  def `collection as repeated parameter` = {
    check(
      Test.testSuccess(
        RepeatedParameters.acceptCollectionRepeatedParameters _,
        "3 eggs can give you List(a delicious sandwich, protein, high cholesterol)" :: HNil
      )
    )
  }

  def `we can spread a collection into positional parameters` = {
    check(
      Test.testSuccess(
        RepeatedParameters.expandCollectionRepeatedParameters _,
        "3 eggs can give you a delicious sandwich, protein, high cholesterol" :: HNil
      )
    )
  }
}
