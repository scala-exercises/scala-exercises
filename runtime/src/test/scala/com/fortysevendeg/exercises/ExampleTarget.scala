/*
 *  scala-exercises
 *
 *  Copyright 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
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

package org.scalaexercises.runtime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

object ExampleTarget extends AnyFlatSpec with Matchers {
  def intStringMethod(a: Int, b: String): String =
    s"$a$b"

  def isOne(a: Int) =
    a shouldBe 1

  class ExampleException extends Exception("this is an example exception")

  def throwsExceptionMethod(): Unit =
    throw new ExampleException

  def takesEitherMethod(either: Either[_, _]): Boolean = true
}
