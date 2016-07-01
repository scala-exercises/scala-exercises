package org.scalaExercises.exercises
package compiler

import org.scalatest._
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.Global

class SourceTextExtractionSpec extends FunSpec with Matchers with Inside {

  describe("source text extraction") {

    val code = """
      /** This is a comment that gets ignored */
      import scala.collection.immutable.{ Seq => Seqq }
      package myPackage {
        import scala.collection._
        /** This is Foo */
        class Foo { val value = 1 }
        /** This is Bar */
        object Bar {
          /** This is Bar.bar */
          def bar() {}
          import Seqq.empty
          object fizz {
            /** This is SubBar */
            object SubBar {
              /** This is SubBar.subbar */
              def subbar() {}
            }
          }
        }
        import scala.io._
      }
      """

    val res = new SourceTextExtraction().extractAll(code :: Nil, List(""), "/")

    it("should find all doc comments on classes and objects") {
      res.comments.map { case (k, v) ⇒ k.mkString(".") → v.raw } should equal(Map(
        "myPackage.Bar.fizz.SubBar" → "/** This is SubBar */",
        "myPackage.Bar.fizz.SubBar.subbar" → "/** This is SubBar.subbar */",
        "myPackage.Bar.bar" → "/** This is Bar.bar */",
        "myPackage.Bar" → "/** This is Bar */",
        "myPackage.Foo" → "/** This is Foo */"
      ))
    }

    /*
    it("should capture imports at static scopes") {
      res.imports.map { case (k, v) ⇒ k.mkString(".") → v.imports.map(renderImport).mkString(";") } should equal(Map(
        "myPackage.Bar" → "Seqq.{ empty }",
        "" → "scala.collection.immutable.{ Seq => Seqq }"
      ))
    }
    */

  }

  describe("capturing imports") {

    it("isolates imports to a given source file") {
      val source1 = """
        import a._
        import b._
        object Object1 {
          /** Method */
          def method() {
          }
        }
        """

      val source2 = """
        import c._
        import d._

        /** Object 2
          * @param name Object 2
          */
        object Object2 {
          import obj2._
          /** Method */
          def method() {
          }
        }
        """

      val paths = List("", "")
      val res = new SourceTextExtraction().extractAll(source1 :: source2 :: Nil, paths, "/")

      // Should capture exactly 1 import for Object1.method
      inside(res.methods.get("Object1" :: "method" :: Nil)) {
        case Some(method) ⇒
          method.imports shouldEqual List("import a._", "import b._")
      }

      // Should capture exactly 2 imports for Object2.method
      inside(res.methods.get("Object2" :: "method" :: Nil)) {
        case Some(method) ⇒
          method.imports shouldEqual List("import c._", "import d._", "import obj2._")
      }

    }

  }

  def renderImportSelector(sel: Global#ImportSelector): String =
    (sel.name, sel.rename) match {
      case (a, b) if a == b ⇒ a.toString
      case (a, b)           ⇒ s"$a => $b"
    }

  def renderImport(imp: Global#Import): String = {
    s"""${imp.expr.toString}.{ ${imp.selectors.map(renderImportSelector).mkString(",")} }"""
  }

}
