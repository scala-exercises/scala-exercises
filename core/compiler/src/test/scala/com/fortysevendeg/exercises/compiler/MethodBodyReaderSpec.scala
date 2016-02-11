package com.fortysevendeg.exercises
package compiler

import org.scalatest._

import scala.reflect.internal.util.AbstractFileClassLoader
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.io.{ VirtualDirectory, AbstractFile }

import java.lang.ClassLoader

class MethodBodyReaderSpec extends FunSpec with Matchers with MethodBodyReaderSpecUtilities {

  describe("code snippet extraction") {
    ignore("should extract a one line snippet and trim all whitespace") {
      val code = """
       |/** This is an example exercise.
       |  * What value returns two?
       |  */
       |def addOne(value: Int) = {
       |  value + value
       |}
       """.stripMargin
    }

    ignore("should extract a multi line snippet and trim all whitespace") {
      val code = """
       |/** This is an example exercise.
       |  * What value returns two?
       |  */
       |def addOne(value: Int) = {
       |  val foo = value + 1
       |  println("we have a foo " + foo)
       |  1 +
       |}
       """.stripMargin
    }

  }

}

trait MethodBodyReaderSpecUtilities {

  val global = new Global(new Settings {
    embeddedDefaults[CompilerSpec]
  }) {
    locally { new Run() }
  }
  val outputTarget = new VirtualDirectory("(memory)", None)
  global.settings.outputDirs.setSingleOutput(outputTarget)

  import global._

  def unwrapBody(tree: Tree): Tree = tree match {
    case q"def $tname(...$paramss): $tpt = $expr" ⇒ expr
    case DocDef(comment, defTree)                 ⇒ unwrapBody(defTree)
    case _                                        ⇒ EmptyTree
  }

  def compileMethod(code: String): Tree = {
    def wrap(code: String): String = s"""package Code { object Code { $code }}"""
    def unwrap(tree: Tree): Tree = tree match {
      case q"package Code { object Code { $statements }}" ⇒ statements
      case _ ⇒ EmptyTree
    }
    unwrap(global
      .newUnitParser(wrap(code))
      .compilationUnit())
  }

  def extractSnippet(code: String): String = {
    val method = compileMethod(code)
    val body = unwrapBody(method)
    MethodBodyReader.read(global)(body)
  }

}
