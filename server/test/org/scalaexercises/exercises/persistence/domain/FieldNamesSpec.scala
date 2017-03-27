/*
 * scala-exercises - server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.persistence.domain

import org.scalatest.{Matchers, WordSpec}

class FieldNamesSpec extends WordSpec with Matchers {

  "persistence domain fieldNames" should {
    "return a lisf with the field names for a case class" in {
      case class Foo(a: String, b: String, c: String)
      val fields = List("a", "b", "c")

      fieldNames[Foo]() shouldBe fields
    }
  }

}
