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

package org.scalaexercises.compiler

import org.scalatest._
import scala.tools.nsc.Global

class SourceTextExtractionSpec extends FunSpec with Matchers with Inside {

  private val innerClassCode: String =
    """/** This is Foo */
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
        import scala.io._"""

  describe("source text extraction") {

    val code = s"""
      /** This is a comment that gets ignored */
      import scala.collection.immutable.{ Seq => Seqq }
      package myPackage {
        import scala.collection._
        $innerClassCode
      }
      """

    val res = new SourceTextExtraction().extractAll(code :: Nil, List(""), "/")

    it("should find all doc comments on classes and objects") {
      res.comments.map { case (k, v) ⇒ k.mkString(".") → v.raw } should equal(
        Map(
          "myPackage.Bar.fizz.SubBar"        → "/** This is SubBar */",
          "myPackage.Bar.fizz.SubBar.subbar" → "/** This is SubBar.subbar */",
          "myPackage.Bar.bar"                → "/** This is Bar.bar */",
          "myPackage.Bar"                    → "/** This is Bar */",
          "myPackage.Foo"                    → "/** This is Foo */"
        ))
    }
  }

  describe("source text extraction with multi-level deep package") {

    val multiLevelPackage = "com.my.nestedPackage"
    val code              = s"""
      /** This is a comment that gets ignored */
      import scala.collection.immutable.{ Seq => Seqq }
      package $multiLevelPackage {
        import scala.collection._
        $innerClassCode
      }
               """

    val res = new SourceTextExtraction().extractAll(code :: Nil, List(""), "/")

    it("should find all doc comments on classes and objects") {
      val comments = res.comments
      comments shouldNot be(empty)
      comments foreach {
        case (k, _) ⇒
          multiLevelPackage.split('.') foreach (k should contain(_))
      }
      comments map { case (k, v) ⇒ k.mkString(".") → v.raw } should equal(
        Map(
          s"$multiLevelPackage.Bar.fizz.SubBar"        → "/** This is SubBar */",
          s"$multiLevelPackage.Bar.fizz.SubBar.subbar" → "/** This is SubBar.subbar */",
          s"$multiLevelPackage.Bar.bar"                → "/** This is Bar.bar */",
          s"$multiLevelPackage.Bar"                    → "/** This is Bar */",
          s"$multiLevelPackage.Foo"                    → "/** This is Foo */"
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
      val res   = new SourceTextExtraction().extractAll(source1 :: source2 :: Nil, paths, "/")

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

  def renderImport(imp: Global#Import): String =
    s"""${imp.expr.toString}.{ ${imp.selectors.map(renderImportSelector).mkString(",")} }"""

}
