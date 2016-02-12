package com.fortysevendeg.exercises
package compiler

import cats.data.Xor

import org.scalatest._

class DocParserSpec extends FunSpec with Matchers {
  import DocParser._

  val content1 = "This is comment content 1"
  val content2 = "This is comment content 2"
  val content3 = "Hello World!!!!!!!"

  def joinWithSpaces(content: String*) =
    content.mkString(" ")

  describe("library comment parsing") {

    it("fails when no description is provided") {
      val comment = """
      /** Library Name
        *
        */
      """
      assert(DocParser.parseLibraryDocComment(comment).isLeft)
    }

    it("captures the first line as name and the rest as description") {
      val comment = s"""
      /** $content1
        * $content2
        * $content3
        */
      """
      val res = DocParser.parseLibraryDocComment(comment)
      res should equal(Xor.right(ParsedLibraryComment(
        name = content1,
        description = joinWithSpaces(content2, content3)
      )))
    }

  }

  describe("line cleaning") {

    it("strips comment container and leading/trailing whitespace") {

      {
        info("leading line before comment")
        val comment =
          s"""|
              |/** $content1 */""".stripMargin

        val res = DocParser.cleanLines(comment)
        res should equal(content1 :: Nil)
      }

      {
        info("leading line before comment")
        info("and leading line in comment")
        val comment =
          s"""|
              |/**
              |  * $content1 */""".stripMargin

        val res = DocParser.cleanLines(comment)
        res should equal(content1 :: Nil)
      }

      {
        info("trailing line after comment")
        val comment =
          s"""|/** $content1 */
              |""".stripMargin

        val res = DocParser.cleanLines(comment)
        res should equal(content1 :: Nil)
      }

      {
        info("trailing line in comment")
        info("and trailing line after comment")

        val comment =
          s"""|/** $content1
              |  */
              |""".stripMargin

        val res = DocParser.cleanLines(comment)
        res should equal(content1 :: Nil)
      }

      {
        info("leading line before comment")
        info("and trailing lines after comment")
        info("with multiple comment lines")

        val comment =
          s"""|
              |/** $content1
              |  * $content2
              |  */
              |
              |
              |""".stripMargin

        val res = DocParser.cleanLines(comment)
        res should equal(content1 :: content2 :: Nil)
      }

      {
        info("leading line before comment")
        info("and trailing lines after comment")
        info("with multiple comment lines")
        info("and empty lines between the comments")

        val comment =
          s"""|
              |/** $content1
              |  *
              |  *
              |  * $content2 */
              |
              |
              |""".stripMargin

        val res = DocParser.cleanLines(comment)
        res should equal(content1 :: "" :: "" :: content2 :: Nil)
      }

      {
        info("leading line before comment")
        info("and trailing lines after comment")
        info("with multiple comment lines")
        info("and empty lines between the comments")
        info("and trailing line in comment")

        val comment =
          s"""|
              |/** $content1
              |  *
              |  *
              |  * $content2
              |  */
              |
              |
              |""".stripMargin

        val res = DocParser.cleanLines(comment)
        res should equal(content1 :: "" :: "" :: content2 :: Nil)
      }

      {
        info("leading lines before comment")
        info("and trailing lines after comment")
        info("with multiple comment lines")
        info("and empty lines between the comments")
        info("and leading lines in the comment")
        info("and trailing line in comment")

        val comment =
          s"""|
              |/** $content1
              |  *
              |  *
              |  * $content2
              |  * $content3
              |  */
              |
              |
              |""".stripMargin

        val res = DocParser.cleanLines(comment)
        res should equal(content1 :: "" :: "" :: content2 :: content3 :: Nil)
      }
    }
  }
}
