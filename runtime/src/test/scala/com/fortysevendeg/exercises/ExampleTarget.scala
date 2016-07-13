package org.scalaexercises.runtime

import org.scalatest._

object ExampleTarget extends FlatSpec with Matchers {
  def intStringMethod(a: Int, b: String): String = {
    s"$a$b"
  }

  def isOne(a: Int) = {
    a shouldBe 1
  }

  class ExampleException extends Exception("this is an example exception")

  def throwsExceptionMethod() {
    throw new ExampleException
  }

  def takesXorMethod(xor: cats.data.Xor[_, _]): Boolean = true
}
