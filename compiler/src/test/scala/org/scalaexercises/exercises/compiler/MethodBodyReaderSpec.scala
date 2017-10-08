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

class MethodBodyReaderSpec extends FunSpec with Matchers with MethodBodyReaderSpecUtilities {

  describe("code snippet extraction") {
    it("should extract a one line snippet and trim all whitespace") {
      val code = """
       |/** This is an example exercise.
       |  * What value returns two?
       |  */
       |def addOne(value: Int) = {
       |  value + value
       |}
       """.stripMargin

      extractSnippet(code) should equal(
        """value + value"""
      )
    }

    it("should extract a multi line snippet and trim all whitespace") {
      val code = """
       |/** This is an example exercise.
       |  * What value returns two?
       |  */
       |def addOne(value: Int) = {
       |
       |  val foo = value + 1
       |    println("we have a foo " + foo)
       |  1 + whatever
       |}
       """.stripMargin

      extractSnippet(code) should equal(
        """|val foo = value + 1
           |  println("we have a foo " + foo)
           |1 + whatever""".stripMargin
      )
    }

    it("should handle the closing block bracket on the last line of code") {
      val code = """
       |/** This is an example exercise.
       |  * What value returns two?
       |  */
       |def addOne(value: Int) = {
       |
       |  val foo = value + 1
       |    println("we have a foo " + foo)
       |  1 + whatever}
       """.stripMargin

      extractSnippet(code) should equal(
        """|val foo = value + 1
           |  println("we have a foo " + foo)
           |1 + whatever""".stripMargin
      )
    }

    it("should handle the opening block bracket on the first line of code") {
      val code = """
       |/** This is an example exercise.
       |  * What value returns two?
       |  */
       |def addOne(value: Int) = {val foo = value + 1
       |  println("we have a foo " + foo)
       |  1 + whatever
       |}
       """.stripMargin

      extractSnippet(code) should equal(
        """|val foo = value + 1
           |  println("we have a foo " + foo)
           |  1 + whatever""".stripMargin
      )
    }

    it("should prefix whitespace in lines that are empty or all whitespace") {
      val code = """
       |/** This is an example exercise.
       |  * What value returns two?
       |  */
       |def addOne(value: Int) = {
       |    println("this is the first line")
       |
       |
       |  println("this is the last line")
       |}
       """.stripMargin

      extractSnippet(code) should equal(
        """|  println("this is the first line")
           |
           |
           |println("this is the last line")""".stripMargin
      )
    }

    it("should extract while taking into account syntax sugar") {
      val code = """
                   |/** This is an example exercise.
                   |  * What value returns six?
                   |  */
                   |def findSix(value: Int) =
                   |  (List(2,4,6) compose List(1,2,3)).apply(value)
                   |
                 """.stripMargin

      extractSnippet(code) should equal(
        """(List(2,4,6) compose List(1,2,3)).apply(value)"""
      )
    }
  }

}

trait MethodBodyReaderSpecUtilities {
  val global = new DocExtractionGlobal() {
    locally { new Run() }
  }

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
      case _                                              ⇒ EmptyTree
    }
    unwrap(
      global
        .newUnitParser(wrap(code))
        .compilationUnit())
  }

  def extractSnippet(code: String): String = {
    val method = compileMethod(code)
    val body   = unwrapBody(method)
    MethodBodyReader.read(global)(body)
  }

}
