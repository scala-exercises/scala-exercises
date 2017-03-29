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

import org.scalacheck.Arbitrary

import cats.Id
import cats.laws.discipline.FunctorTests

class CommentZedSpec extends FunSpec {
  import CommentZed._

  describe("Empty comment algebra") {

    implicit def emptyArbitrary[A]: Arbitrary[Empty[A]] =
      Arbitrary(Empty)

    it("should pass Functor laws") {
      val rs = FunctorTests[Empty].functor[Int, Int, Int]
      rs.all
    }

  }

  describe("Ignore comment algebra") {

    implicit def emptyIgnore[A]: Arbitrary[Ignore[A]] =
      Arbitrary(Ignore)

    it("should pass Functor laws") {
      val rs = FunctorTests[Ignore].functor[Int, Int, Int]
      rs.all
    }

  }

}

class CommentParsingRenderingSpec extends FunSpec with Matchers with Inside {
  import CommentZed._
  import Comments.Mode

  private[this] val content1 = "This is comment content 1"
  private[this] val content2 = "This is comment content 2"
  private[this] val content3 = "Hello World!!!!!!!"

  private[this] val global = new DocExtractionGlobal() {
    locally { new Run() }
  }

  private[this] val commentFactory = CommentFactory(global)

  private[this] def parse(text: String) = commentFactory.parse(text.stripMargin)

  object comments {

    lazy val name        = "ThisIsMyName"
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
        inside(CommentParsing.parse[Mode.Name[Id]](comment)) {
          case Left(_) ⇒
        }
        inside(CommentParsing.parse[Mode.Name[Option]](comment)) {
          case Right(parsed) ⇒
            parsed.name shouldEqual None
        }
        inside(CommentParsing.parse[Mode.Name[Empty]](comment)) {
          case Right(parsed) ⇒
            parsed.name shouldEqual Empty
        }

      }

      comments.withNames.foreach { comment ⇒
        inside(CommentParsing.parse[Mode.Name[Id]](comment)) {
          case Right(parsed) ⇒
            parsed.name shouldEqual comments.name
        }
        inside(CommentParsing.parse[Mode.Name[Option]](comment)) {
          case Right(parsed) ⇒
            parsed.name shouldEqual Some(comments.name)
        }
        inside(CommentParsing.parse[Mode.Name[Empty]](comment)) {
          case Left(_) ⇒
        }

      }

      // TODO: Handle invalid scenarious
      /*
      comments.invalidNames.foreach { comment ⇒

        inside(CommentParsing.parse[Mode.Name[Id]](comment)) {
          case Left(_) ⇒
        }
        inside(CommentParsing.parse[Mode.Name[Option]](comment)) {
          case Left(_) ⇒
        }
        inside(CommentParsing.parse[Mode.Name[Empty]](comment)) {
          case Left(_) ⇒
        }
      }
     */

    }

    it("properly handles descriptions") {

      comments.withoutDescriptions.foreach { comment ⇒
        inside(CommentParsing.parse[Mode.Description[Id]](comment)) {
          case Left(_) ⇒
        }
        inside(CommentParsing.parse[Mode.Description[Option]](comment)) {
          case Right(parsed) ⇒
            parsed.description shouldEqual None
        }
        inside(CommentParsing.parse[Mode.Description[Empty]](comment)) {
          case Right(parsed) ⇒
            parsed.description shouldEqual Empty
        }

      }

      comments.withDescriptions.foreach { comment ⇒
        inside(CommentParsing.parse[Mode.Description[Id]](comment)) {
          case Right(parsed) ⇒
        }
        inside(CommentParsing.parse[Mode.Description[Option]](comment)) {
          case Right(parsed) ⇒
            parsed.description shouldBe defined
        }
        inside(CommentParsing.parse[Mode.Description[Empty]](comment)) {
          case Left(_) ⇒
        }
      }

    }

    it("properly handles explanations") {

      comments.withoutExplanations.foreach { comment ⇒
        inside(CommentParsing.parse[Mode.Explanation[Id]](comment)) {
          case Left(_) ⇒
        }
        inside(CommentParsing.parse[Mode.Explanation[Option]](comment)) {
          case Right(parsed) ⇒
            parsed.explanation shouldEqual None
        }
        inside(CommentParsing.parse[Mode.Explanation[Empty]](comment)) {
          case Right(parsed) ⇒
            parsed.explanation shouldEqual Empty
        }

      }

      comments.withExplanations.foreach { comment ⇒
        inside(CommentParsing.parse[Mode.Explanation[Id]](comment)) {
          case Right(parsed) ⇒
        }
        inside(CommentParsing.parse[Mode.Explanation[Option]](comment)) {
          case Right(parsed) ⇒
            parsed.explanation shouldBe defined
        }
        inside(CommentParsing.parse[Mode.Explanation[Empty]](comment)) {
          case Left(_) ⇒
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

      val rendered = Comments.parseAndRender[Mode.Library](comment)

      assert(rendered.isRight, s", $rendered")
    }

  }

}
