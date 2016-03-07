package stdlib

import shapeless._

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

  def success[P <: Product, L <: HList](method: Function[P, Unit], answer: L)(
    implicit
    T: shapeless.ops.hlist.Tupler[L],
    A: Arbitrary[L]
  ): Prop = {
    val rightGen: Gen[P] = genTuple(answer).asInstanceOf[Gen[P]]
    val rightProp = forAll(rightGen)({ p ⇒
      try {
        method(p)
        true
      } catch {
        case _: Throwable ⇒ false
      }
    })

    val wrongGen: Gen[P] = genNotTuple(answer).asInstanceOf[Gen[P]]
    val wrongProp = forAll(wrongGen)({ p ⇒
      try {
        method(p)
        false
      } catch {
        case _: Throwable ⇒ true
      }
    })

    Prop.all(rightProp, wrongProp)
  }

  def genTuple[L <: HList](l: L)(
    implicit
    tupler: shapeless.ops.hlist.Tupler[L]
  ): Gen[_] = {
    Gen.const(tupler(l))
  }

  def genNotTuple[L <: HList](l: L)(
    implicit
    T: shapeless.ops.hlist.Tupler[L],
    A: Arbitrary[L]
  ): Gen[_] = {
    A.arbitrary.suchThat(_ != l)
  }

  // TODO: Better way to pass method reference, this boilerplate is ridiculous
  def `we can generate right and wrong props` = {
    val aProp = success({ foo: (Int, Int) ⇒ new AClass().aMethod _ tupled (foo) }, 1 :: 2 :: HNil)
    check(aProp)
  }
}
