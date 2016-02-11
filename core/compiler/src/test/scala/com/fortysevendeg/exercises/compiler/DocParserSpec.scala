package com.fortysevendeg.exercises
package compiler

import org.scalatest._

class DocParserSpec extends FunSpec with Matchers {
  describe("Exercise comments") {
    it("can be parsed even when having trailing whitespace") {
      val comment = """
      /** The next line has a trailing whitespace
        *
        */
      """
      assert(DocParser.parseLibraryDocComment(comment).isRight)
    }
  }
}
