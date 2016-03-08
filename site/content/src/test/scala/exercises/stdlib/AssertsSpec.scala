package stdlib

import shapeless._
import shapeless.ops.function._

import org.scalacheck.{ Prop, Arbitrary }
import org.scalacheck.Gen
import Prop.forAll

import org.scalatest._
import org.scalatest.prop._
import org.scalatest.Matchers
import Matchers._

import org.scalacheck.Shapeless._

class AssertsSpec
    extends Spec
    with Checkers {

  class AClass extends Spec with Matchers {
    def aMethod(res0: Int, res1: Int) = {
      res0 shouldBe 1
      res1 shouldBe 2
    }
  }

  def success[F, R, L <: HList](method: F, answer: L)(
    implicit
    A:     Arbitrary[L],
    fntop: FnToProduct.Aux[F, L ⇒ R]
  ): Prop = {
    val rightGen = genRightAnswer(answer)
    val rightProp = forAll(rightGen)({ p ⇒
      try {
        fntop(method)(p)
        true
      } catch {
        case _: Throwable ⇒ false
      }
    })

    val wrongGen = genWrongAnswer(answer)
    val wrongProp = forAll(wrongGen)({ p ⇒
      try {
        fntop(method)(p)
        false
      } catch {
        case _: Throwable ⇒ true
      }
    })

    Prop.all(rightProp, wrongProp)
  }

  def genRightAnswer[L <: HList](answer: L): Gen[L] = {
    Gen.const(answer)
  }

  def genWrongAnswer[L <: HList](l: L)(
    implicit
    A: Arbitrary[L]
  ): Gen[L] = {
    A.arbitrary.suchThat(_ != l)
  }

  val theInstance = new AClass()

  def `we can generate right and wrong props` = {
    val aProp = success(theInstance.aMethod _, 1 :: 2 :: HNil)
    check(aProp)
  }
}
