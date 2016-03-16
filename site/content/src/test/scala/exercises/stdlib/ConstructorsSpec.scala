package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class ConstructorsSpec extends Spec with Checkers {
  def `primary constructor` = {
    check(
      Test.testSuccess(
        Constructors.primaryConstructorConstructors _,
        "defaultname" :: HNil
      )
    )
  }
}
