package com.fortysevendeg.exercises
package compiler

import org.scalatest._
import scala.reflect.internal.util.BatchSourceFile

class SourceTextExtractionSpec extends FunSpec with Matchers {

  val code = """
    /** This is a comment that gets ignored */
    package myPackage {
      /** This is Foo */
      class Foo { val value = 1 }
      /** This is Bar */
      object Bar {
        /** This is Bar.bar */
        def bar() {}
        object fizz {
          /** This is SubBar */
          object SubBar {
            /** This is SubBar.subbar */
            def subbar() {}
          }
        }
      }
    }
    """

  describe("doc comment searching") {

    it("should find all doc comments on classes and objects") {

      val res = new SourceTextExtraction().extractAllComments(code :: Nil)
        .map { case (k, v) ⇒ k.mkString(".") → v }

      res should equal(Map(
        "myPackage.Bar.fizz.SubBar" → "/** This is SubBar */",
        "myPackage.Bar.fizz.SubBar.subbar" → "/** This is SubBar.subbar */",
        "myPackage.Bar.bar" → "/** This is Bar.bar */",
        "myPackage.Bar" → "/** This is Bar */",
        "myPackage.Foo" → "/** This is Foo */"
      ))

    }

  }
}
