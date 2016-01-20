package com.fortysevendeg.exercises
package compiler

import org.scalatest._
import scala.reflect.internal.util.BatchSourceFile
import scala.util.Try

import scala.tools.nsc._
import scala.tools.nsc.doc.{ Settings ⇒ _, _ }

class DocExtractionGlobalSpec extends FunSpec with Matchers {

  val code = """
    /** This is a comment that gets ignored */
    package myPackage {
      /** This is Foo */
      object Foo { val value = 1 }
      /** This is Bar */
      object Bar {
        /** This is Bar.bar */
        def bar() {}
      }
    }
    """

  describe("doc comment searching") {
    it("should find doc comments on objects") {

      val g = new DocExtractionGlobal()

      new g.Run() compileSources List(
        new BatchSourceFile("newSource", code)
      )

      val currentRun = g.currentRun
      val rootTree = currentRun.units.toList.head.body

      val res = DocCommentFinder.findByName(g)(rootTree, "myPackage.Foo" :: "myPackage.Bar" :: Nil)
        .mapValues(_.raw)

      res should equal(Map(
        "myPackage.Foo" → "/** This is Foo */",
        "myPackage.Bar" → "/** This is Bar */"
      ))

    }

    it("should do the same via the java facade") {

      val global = new DocExtractionGlobal.Java()
      val res = global.find(code, Array("myPackage.Foo", "doesnt.exit", "myPackage.Bar"))
      res.toList should equal(List(
        "/** This is Foo */",
        null,
        "/** This is Bar */"
      ))

    }

  }
}
