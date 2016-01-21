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

      val g = new DocExtractionGlobal()

      new g.Run() compileSources List(
        new BatchSourceFile("newSource", code)
      )

      val currentRun = g.currentRun
      val rootTree = currentRun.units.toList.head.body

      val res = DocCommentFinder.findAll(g)(rootTree)
        .map { case (k, v) ⇒ k.mkString(".") → v.raw }
        .toMap

      res should equal(Map(
        "myPackage.Bar.fizz.SubBar" → "/** This is SubBar */",
        "myPackage.Bar.fizz.SubBar.subbar" → "/** This is SubBar.subbar */",
        "myPackage.Bar.bar" → "/** This is Bar.bar */",
        "myPackage.Bar" → "/** This is Bar */",
        "myPackage.Foo" → "/** This is Foo */"
      ))

    }

    ignore("find all doc comments on classes and objects via the java façade") {
      import scala.collection.JavaConverters._

      val global = new DocExtractionGlobal.Java()
      val res = global.findAll(code)
      res.asScala should equal(Map(
        "myPackage.Bar.fizz.SubBar" → "/** This is SubBar */",
        "myPackage.Bar.fizz.SubBar.subbar" → "/** This is SubBar.subbar */",
        "myPackage.Bar.bar" → "/** This is Bar.bar */",
        "myPackage.Bar" → "/** This is Bar */",
        "myPackage.Foo" → "/** This is Foo */"
      ))

    }

    ignore("should find doc comments on objects") {

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

    ignore("should do the same via the java facade") {

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
