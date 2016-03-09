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
      Test.success(
        CaseClasses.caseClassesSupportEquality _,
        false :: true :: false :: false :: HNil
      )
    )
  }

  def `hash codes` = {
    check(
      Test.success(
        CaseClasses.hashcodeMethodCaseClasses _,
        false :: true :: HNil
      )
    )
  }

  def `case class creation` = {
    check(
      Test.success(
        CaseClasses.creationCaseClasses _,
        true :: false :: false :: HNil
      )
    )
  }

  def `to string method` = {
    check(
      Test.success(
        CaseClasses.toStringMethodCaseClasses _,
        "Dog(Scooby,Doberman)" :: HNil
      )
    )
  }

  def `properties` = {
    check(
      Test.success(
        CaseClasses.propertiesCaseClasses _,
        "Scooby" :: "Doberman" :: HNil
      )
    )
  }

  def `mutable properties` = {
    check(
      Test.success(
        CaseClasses.mutablePropertiesCaseClasses _,
        "Scooby" :: "Doberman" :: "Scooby Doo" :: "Doberman" :: HNil
      )
    )
  }

  def `altering case classes` = {
    check(
      Test.success(
        CaseClasses.alteringCaseClasses _,
        "Scooby" :: "Doberman" :: "Scooby Doo" :: "Doberman" :: HNil
      )
    )
  }

  def `case class parameters` = {
    check(
      Test.success(
        CaseClasses.parametersCaseClasses _,
        "Fred" :: "Jones" :: 23 :: "111-22-3333" ::
          "Samantha" :: "Jones" :: 0 :: "" ::
          "Fred" :: "Jones" :: 0 :: "111-22-3333" :: true :: HNil
      )
    )
  }

  def `case classes as tuples` = {
    check(
      Test.success(
        CaseClasses.asTupleCaseClasses _,
        "Fred" :: "Jones" :: 23 :: "111-22-3333" :: HNil
      )
    )
  }

  def `case classes are serializable` = {
    check(
      Test.success(
        CaseClasses.serializableCaseClasses _,
        true :: false :: HNil
      )
    )
  }
}
