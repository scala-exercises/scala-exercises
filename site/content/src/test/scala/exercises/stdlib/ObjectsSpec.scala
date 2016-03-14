package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

import org.scalacheck.Shapeless._

class ObjectsSpec extends Spec with Checkers {
  def `singleton` = {
    check(
      Test.testSuccess(
        Objects.singletonObjects _,
        "Hi" :: "Hola" :: "Hallo" :: "Szia" :: HNil
      )
    )
  }

  def `they are unique` = {
    check(
      Test.testSuccess(
        Objects.notStaticMethodObjects _,
        true :: true :: HNil
      )
    )
  }

  def `companion objects` = {
    check(
      Test.testSuccess(
        Objects.companionObjectObjects _,
        "Grand Hotel" :: HNil
      )
    )
  }

  // FIXME: can't test properly because it relies on a shared mutable var :_)
  // def `shared variables` = {
  //   check(
  //     Test.testSuccess(
  //       Objects.sharingVariablesObjects _,
  //       1500 :: HNil
  //     )
  //   )
  // }

  def `private members` = {
    check(
      Test.testSuccess(
        Objects.privateValuesObjects _,
        "Superman" :: "Spiderman" :: "Batman" :: "Wonder Woman" :: HNil
      )
    )
  }
}
