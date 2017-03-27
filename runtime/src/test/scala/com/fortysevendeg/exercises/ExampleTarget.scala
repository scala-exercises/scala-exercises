/*
 * scala-exercises - runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.runtime

import org.scalatest._

object ExampleTarget extends FlatSpec with Matchers {
  def intStringMethod(a: Int, b: String): String =
    s"$a$b"

  def isOne(a: Int) =
    a shouldBe 1

  class ExampleException extends Exception("this is an example exception")

  def throwsExceptionMethod() {
    throw new ExampleException
  }

  def takesEitherMethod(either: Either[_, _]): Boolean = true
}
