package com.fortysevendeg.exercises
package compiler

import org.scalatest._

class CodeSnippetSpec extends FunSpec with Matchers with CodeSnippetUtilities {

  describe("code snippet extraction") {
    it("should extract a one line snippet and trim all whitespace") {

      val code = """
       |/** This is an example exercise.
       |  * What value returns two?
       |  */
       |def addOne(value: Int) = {
       |  value + 1
       |}
       """.stripMargin

      extractSnippet(code) should be("value + 1")

    }

    it("should extract a multiline snippet and trim consistent prefix whitespace") {

      def check(code: String) = new {
        def against(result: String) = extractSnippet(code.stripMargin) should be(result.stripMargin.trim)
      }
      info("all uniform")

      check("""
       |/** Example.
       |  */
       |def exampleExercise(value: Int) = {
       |  println("avoid")
       |  printlt("certain")
       |  println("death")
       |}""")
        .against("""
          |println("avoid")
          |printlt("certain")
          |println("death")
          """)

      info("extra space line 1")

      check("""
       |/** Example.
       |  */
       |def exampleExercise(value: Int) = {
       |   println("avoid")
       |  printlt("certain")
       |  println("death")
       |}""")
        .against("""
          | println("avoid")
          |printlt("certain")
          |println("death")
          """)

      info("extra space line 2")

      check("""
       |/** Example.
       |  */
       |def exampleExercise(value: Int) = {
       |  println("avoid")
       |   printlt("certain")
       |  println("death")
       |}""")
        .against("""
          |println("avoid")
          | printlt("certain")
          |println("death")
          """)

      info("extra space line 3")

      check("""
       |/** Example.
       |  */
       |def exampleExercise(value: Int) = {
       |  println("avoid")
       |  printlt("certain")
       |   println("death")
       |}""")
        .against("""
          |println("avoid")
          |printlt("certain")
          | println("death")
          """)

      info("various extra space")

      check("""
       |/** Example.
       |  */
       |def exampleExercise(value: Int) = {
       |      println("avoid")
       |    printlt("certain")
       |     println("death")
       |}""")
        .against("""
          |  println("avoid")
          |printlt("certain")
          | println("death")
          """)

    }

  }

}

trait CodeSnippetUtilities {

  val parser = ScalaExerciseParser()
  import parser.compiler._

  // method names are not obvious
  // TODO: rename them

  def wrapObject(code: String): String =
    s"""package Code { object Code {
      $code
    }}"""

  def unwrapObject(tree: Tree): Tree = tree match {
    case q"package Code { object Code { $statements }}" ⇒
      statements
    case _ ⇒ EmptyTree
  }

  def unwrapDef(tree: Tree): Tree = tree match {
    case DocDef(comment, q"def $tname(...$paramss): $tpt = $expr") ⇒
      expr
  }

  def compileDef(code: String): Tree = {
    val rawTree = parser.compiler
      .newUnitParser(wrapObject(code))
      .compilationUnit()
    unwrapObject(rawTree)
  }

  def extractSnippet(code: String): String =
    parser.readCleanExpr(unwrapDef(compileDef(code)))

}
