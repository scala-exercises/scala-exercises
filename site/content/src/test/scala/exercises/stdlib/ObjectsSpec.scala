package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class ObjectsSpec extends Spec with Checkers {
  def `singleton` = {
    check(
      Test.success(
        Objects.singletonObjects _,
        "Hi" :: "Hola" :: "Hallo" :: "Szia" :: HNil
      )
    )
  }

  def `they are unique` = {
    check(
      Test.success(
        Objects.notStaticMethodObjects _,
        true :: true :: HNil
      )
    )
  }

  def `companion objects` = {
    check(
      Test.success(
        Objects.companionObjectObjects _,
        "Grand Hotel" :: HNil
      )
    )
  }

  // FIXME: can't test properly because it relies on a shared mutable var :_)
  // def `shared variables` = {
  //   check(
  //     Test.success(
  //       Objects.sharingVariablesObjects _,
  //       1500 :: HNil
  //     )
  //   )
  // }

  def `private members` = {
    check(
      Test.success(
        Objects.privateValuesObjects _,
        "Superman" :: "Spiderman" :: "Batman" :: "Wonder Woman" :: HNil
      )
    )
  }
}
