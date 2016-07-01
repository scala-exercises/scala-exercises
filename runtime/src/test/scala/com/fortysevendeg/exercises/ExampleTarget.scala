package org.scalaexercises.exercises

object ExampleTarget {
  def intStringMethod(a: Int, b: String): String = {
    s"$a$b"
  }

  class ExampleException extends Exception("this is an example exception")

  def throwsExceptionMethod() {
    throw new ExampleException
  }

  def takesXorMethod(xor: cats.data.Xor[_, _]): Boolean = true
}
