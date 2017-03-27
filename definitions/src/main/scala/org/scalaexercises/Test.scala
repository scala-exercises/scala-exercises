/*
 * scala-exercises - definitions
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises

import shapeless._
import shapeless.ops.function._

import cats.implicits._
import org.scalacheck.{Arbitrary, Prop}
import org.scalacheck.Gen
import Prop.forAll

import org.scalatest.exceptions._

import org.scalacheck.Shapeless._

object Test {

  def testSuccess[F, R, L <: HList](method: F, answer: L)(
      implicit A: Arbitrary[L],
      fntop: FnToProduct.Aux[F, L ⇒ R]
  ): Prop = {
    val rightGen = genRightAnswer(answer)
    val rightProp = forAll(rightGen)({ p ⇒
      val result = Either.catchOnly[GeneratorDrivenPropertyCheckFailedException]({
        fntop(method)(p)
      })
      result match {
        case Left(exc) ⇒
          exc.cause match {
            case Some(originalException) ⇒ throw originalException
            case _                       ⇒ false
          }
        case _ ⇒ true
      }
    })

    val wrongGen = genWrongAnswer(answer)
    val wrongProp = forAll(wrongGen)({ p ⇒
      Either.catchNonFatal({ fntop(method)(p) }).isLeft
    })

    Prop.all(rightProp, wrongProp)
  }

  def genRightAnswer[L <: HList](answer: L): Gen[L] =
    Gen.const(answer)

  def genWrongAnswer[L <: HList](l: L)(
      implicit A: Arbitrary[L]
  ): Gen[L] =
    A.arbitrary.suchThat(_ != l)
}
