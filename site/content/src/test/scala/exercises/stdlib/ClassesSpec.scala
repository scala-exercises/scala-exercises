package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class ClassesSpec extends Spec with Checkers {
  def `classes with val parameters` = {
    check(
      Test.testSuccess(
        Classes.classWithValParameterClasses _,
        "Gandalf" :: HNil
      )
    )
  }

  def `classes with var parameters` = {
    check(
      Test.testSuccess(
        Classes.classWithVarParameterClasses _,
        "Flying character" :: "Flying white character" :: HNil
      )
    )
  }
}
