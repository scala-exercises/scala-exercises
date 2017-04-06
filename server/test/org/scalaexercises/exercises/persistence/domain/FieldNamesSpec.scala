package org.scalaexercises.exercises.persistence.domain

import org.scalatest.{ WordSpec, Matchers }

class FieldNamesSpec extends WordSpec with Matchers {

  "persistence domain fieldNames" should {
    "return a lisf with the field names for a case class" in {
      case class Foo(a: String, b: String, c: String)
      val fields = List("a", "b", "c")

      fieldNames[Foo]() shouldBe fields
    }
  }

}