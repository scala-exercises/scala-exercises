package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class CaseClassesSpec extends Spec with Checkers {
  def `case classes comparisons` = {
    check(
      Test.testSuccess(
        CaseClasses.caseClassesSupportEquality _,
        false :: true :: false :: false :: HNil
      )
    )
  }

  def `hash codes` = {
    check(
      Test.testSuccess(
        CaseClasses.hashcodeMethodCaseClasses _,
        false :: true :: HNil
      )
    )
  }

  def `case class creation` = {
    check(
      Test.testSuccess(
        CaseClasses.creationCaseClasses _,
        true :: false :: false :: HNil
      )
    )
  }

  def `to string method` = {
    check(
      Test.testSuccess(
        CaseClasses.toStringMethodCaseClasses _,
        "Dog(Scooby,Doberman)" :: HNil
      )
    )
  }

  def `properties` = {
    check(
      Test.testSuccess(
        CaseClasses.propertiesCaseClasses _,
        "Scooby" :: "Doberman" :: HNil
      )
    )
  }

  def `mutable properties` = {
    check(
      Test.testSuccess(
        CaseClasses.mutablePropertiesCaseClasses _,
        "Scooby" :: "Doberman" :: "Scooby Doo" :: "Doberman" :: HNil
      )
    )
  }

  def `altering case classes` = {
    check(
      Test.testSuccess(
        CaseClasses.alteringCaseClasses _,
        "Scooby" :: "Doberman" :: "Scooby Doo" :: "Doberman" :: HNil
      )
    )
  }

  def `case class parameters` = {
    check(
      Test.testSuccess(
        CaseClasses.parametersCaseClasses _,
        "Fred" :: "Jones" :: 23 :: "111-22-3333" ::
          "Samantha" :: "Jones" :: 0 :: "" ::
          "Fred" :: "Jones" :: 0 :: "111-22-3333" :: true :: HNil
      )
    )
  }

  def `case classes as tuples` = {
    check(
      Test.testSuccess(
        CaseClasses.asTupleCaseClasses _,
        "Fred" :: "Jones" :: 23 :: "111-22-3333" :: HNil
      )
    )
  }

  def `case classes are serializable` = {
    check(
      Test.testSuccess(
        CaseClasses.serializableCaseClasses _,
        true :: false :: HNil
      )
    )
  }
}
