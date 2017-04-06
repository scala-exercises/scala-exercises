/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
