package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class TypeSignaturesSpec extends Spec with Checkers {
  def `type signatures` = {
    check(
      Test.success(
        TypeSignatures.typeSignatureTypeSignatures _,
        true :: HNil
      )
    )
  }

  def `class metainformation` = {
    check(
      Test.success(
        TypeSignatures.retrieveMetaInformationTypeSignatures _,
        "java.lang.String" :: "String" :: HNil
      )
    )
  }

  def `class metainformation derivation` = {
    check(
      Test.success(
        TypeSignatures.deriveMetaInformationTypeSignatures _,
        true :: "java.lang.String" :: "String" :: HNil
      )
    )
  }

  def `instanceof class` = {
    check(
      Test.success(
        TypeSignatures.isInstanceOfFunctionTypeSignatures _,
        true :: HNil
      )
    )
  }

  def `asinstanceof class` = {
    check(
      Test.success(
        TypeSignatures.asInstanceOfFunctionTypeSignatures _,
        true :: HNil
      )
    )
  }
}
