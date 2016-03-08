package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class AssertsSpec extends Spec with Checkers {
  def `scalatest asserts` = {
    check(Test.success(Asserts.scalaTestAsserts _, true :: HNil))
  }

  // FIXME: depends on #259
  // check(success(Asserts.booleansAsserts _, HNil))

  def `values asserts` = {
    check(Test.success(Asserts.valuesAsserts _, 2 :: HNil))
  }
}

class ByNameParameterSpec extends Spec with Checkers {
  def `takes unit by name parameter` = {
    val right: Either[Throwable, Int] = Right(29)

    check(
      Test.success(
        ByNameParameter.takesUnitByNameParameter _,
        right :: HNil
      )
    )
  }

  def `takes parameters by name` = {
    val right: Either[Throwable, Int] = Right(69)

    check(
      Test.success(
        ByNameParameter.byNameParameter _,
        right :: HNil
      )
    )
  }

  def `apply parameters` = {
    check(
      Test.success(
        ByNameParameter.withApplyByNameParameter _,
        "retzelpay" :: HNil
      )
    )
  }
}

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

  // FIXME: Shapeless doesn't find an implicit FnToProduct.Aux, why?
  // def `case class parameters` = {
  //   check(
  //     Test.success(
  //       CaseClasses.parametersCaseClasses _,
  //       "Fred" :: "Jones" :: 23 :: "111-22-3333" ::
  //         "Samantha" :: "Jones" :: 0 :: "111-22-3333" ::
  //         "Fred" :: "Jones" :: 0 :: "111-22-3333" ::
  //         "Fred" :: "Jones" :: 23 :: "111-22-3333" :: HNil
  //     )
  //   )
  // }

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

class ClassesSpec extends Spec with Checkers {

  def `classes with val parameters` = {
    check(
      Test.success(
        Classes.classWithValParameterClasses _,
        "Gandalf" :: HNil
      )
    )
  }

  def `classes with var parameters` = {
    check(
      Test.success(
        Classes.classWithVarParameterClasses _,
        "Flying character" :: "Flying white character" :: HNil
      )
    )
  }
}

class ConstructorsSpec extends Spec with Checkers {
  def `primary constructor` = {
    check(
      Test.success(
        Constructors.primaryConstructorConstructors _,
        "defaultname" :: HNil
      )
    )
  }
}
