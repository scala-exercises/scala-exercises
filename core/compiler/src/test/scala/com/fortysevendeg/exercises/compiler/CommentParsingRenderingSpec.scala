package com.fortysevendeg.exercises
package compiler

import org.scalatest._

import org.scalacheck.Arbitrary

import cats.Id
import cats.data.Xor
import cats.laws.discipline.FunctorTests

class CommentZedSpec extends FunSpec {
  import CommentZed._

  describe("Empty comment algebra") {

    implicit def emptyArbitrary[A]: Arbitrary[Empty[A]] =
      Arbitrary(Empty)

    it("should pass Functor laws") {
      val rs = FunctorTests[Empty].functor[Int, Int, Int]
      rs.all.check
    }

  }

  describe("Ignore comment algebra") {

    implicit def emptyIgnore[A]: Arbitrary[Ignore[A]] =
      Arbitrary(Ignore)

    it("should pass Functor laws") {
      val rs = FunctorTests[Ignore].functor[Int, Int, Int]
      rs.all.check
    }

  }

}

class CommentParsingRenderingSpec extends FunSpec with Matchers with Inside {
  import CommentParsing.ParseMode
  import CommentZed._

  val content1 = "This is comment content 1"
  val content2 = "This is comment content 2"
  val content3 = "Hello World!!!!!!!"

  val global = new DocExtractionGlobal() {
    locally { new Run() }
  }

  val commentFactory = CommentFactory(global)

  def parse(text: String) = commentFactory.parse(text.stripMargin)

  object comments {

    lazy val name = "ThisIsMyName"
    lazy val description = "This is some description </br>! _!_"
    lazy val explanation = "This is some explanation </br>! _!_"

    lazy val withoutNames = List(
      parse("""
        |/**
        |  */""")
    )

    lazy val withNames = List(
      s"""
        |/** @param name $name
        |  */"""
    ) map (parse _)

    lazy val invalidNames = List(
      s"""
        |/** @param name
        |     $name
        |     $name again
        |  */"""

    ) map (parse _)

    lazy val withoutDescriptions = List(
      """
        |/**  */""",

      """
        |/**
        |  */""",

      """
        |/**
        |  *
        |  */"""

    ) map (parse _)

    lazy val withDescriptions = List(
      s"""
        |/** $description */""",

      s"""
        |/** $description
        |  */""",
      s"""
        |/**
        |  * $description
        |  */"""

    ) map (parse _)

    lazy val withoutExplanations = List(
      """
        |/**  */""",

      """
        |/**
        |  */""",

      """
        |/**
        |  *
        |  */"""

    ) map (parse _)

    lazy val withExplanations = List(
      s"""
        |/** @param explanation $explanation */""",

      s"""
        |/** @param explanation $explanation
        |  */""",

      s"""
        |/** @param explanation
        |  * $explanation
        |  */"""

    ) map (parse _)

  }

  describe("doc parsing") {

    it("properly handles names") {

      comments.withoutNames.foreach { comment ⇒

        inside(CommentParsing.parse[ParseMode.Name[Id]](comment)) {
          case Xor.Left(_) ⇒
        }
        inside(CommentParsing.parse[ParseMode.Name[Option]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.name shouldEqual None
        }
        inside(CommentParsing.parse[ParseMode.Name[Empty]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.name shouldEqual Empty
        }

      }

      comments.withNames.foreach { comment ⇒

        inside(CommentParsing.parse[ParseMode.Name[Id]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.name shouldEqual comments.name
        }
        inside(CommentParsing.parse[ParseMode.Name[Option]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.name shouldEqual Some(comments.name)
        }
        inside(CommentParsing.parse[ParseMode.Name[Empty]](comment)) {
          case Xor.Left(_) ⇒
        }

      }

      // TODO: Handle invalid scenarious
      /*
      comments.invalidNames.foreach { comment ⇒

        inside(CommentParsing.parse[ParseMode.Name[Id]](comment)) {
          case Xor.Left(_) ⇒
        }
        inside(CommentParsing.parse[ParseMode.Name[Option]](comment)) {
          case Xor.Left(_) ⇒
        }
        inside(CommentParsing.parse[ParseMode.Name[Empty]](comment)) {
          case Xor.Left(_) ⇒
        }
      }
      */

    }

    it("properly handles descriptions") {

      comments.withoutDescriptions.foreach { comment ⇒

        inside(CommentParsing.parse[ParseMode.Description[Id]](comment)) {
          case Xor.Left(_) ⇒
        }
        inside(CommentParsing.parse[ParseMode.Description[Option]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.description shouldEqual None
        }
        inside(CommentParsing.parse[ParseMode.Description[Empty]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.description shouldEqual Empty
        }

      }

      comments.withDescriptions.foreach { comment ⇒
        inside(CommentParsing.parse[ParseMode.Description[Id]](comment)) {
          case Xor.Right(parsed) ⇒
        }
        inside(CommentParsing.parse[ParseMode.Description[Option]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.description shouldBe defined
        }
        inside(CommentParsing.parse[ParseMode.Description[Empty]](comment)) {
          case Xor.Left(_) ⇒
        }
      }

    }

    it("properly handles explanations") {

      comments.withoutExplanations.foreach { comment ⇒

        inside(CommentParsing.parse[ParseMode.Explanation[Id]](comment)) {
          case Xor.Left(_) ⇒
        }
        inside(CommentParsing.parse[ParseMode.Explanation[Option]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.explanation shouldEqual None
        }
        inside(CommentParsing.parse[ParseMode.Explanation[Empty]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.explanation shouldEqual Empty
        }

      }

      comments.withExplanations.foreach { comment ⇒
        inside(CommentParsing.parse[ParseMode.Explanation[Id]](comment)) {
          case Xor.Right(parsed) ⇒
        }
        inside(CommentParsing.parse[ParseMode.Explanation[Option]](comment)) {
          case Xor.Right(parsed) ⇒
            parsed.explanation shouldBe defined
        }
        inside(CommentParsing.parse[ParseMode.Explanation[Empty]](comment)) {
          case Xor.Left(_) ⇒
        }
      }

    }
  }

  describe("parsing/rendering library comments") {
    it("works -- TODO, improve these") {

      val comment = commentFactory.parse(s"""
        |/** This is my comment
        |  * It's really nice
        |  * @param name Name
        |  */""".stripMargin)

      val rendered = Comments.parseAndRender[ParseMode.Library](comment)

      assert(rendered.isRight, s", $rendered")
    }

  }

}
