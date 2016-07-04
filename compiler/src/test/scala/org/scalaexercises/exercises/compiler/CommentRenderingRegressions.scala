package org.scalaexercises.compiler

import org.scalatest._

import cats.data.Xor
import cats.std.option._

class CommentRenderingRegressions extends FunSpec with Matchers
    with Inside {
  import Comments.Mode

  private[this] val global = new DocExtractionGlobal() {
    locally { new Run() }
  }

  private[this] val commentFactory = CommentFactory(global)

  private[this] def parse(text: String) = commentFactory.parse(text.stripMargin)

  describe("issues") {
    it("github #309") {

      // Summary:
      // Certain code blocks in comments fail to render.
      // In this instance, the buggy result was:
      // <p></p><pre class="scala"><code class="scala"></code></pre>

      val comment = commentFactory.parse(s"""
        /**
          * {{{
          * Functor[List].map(List("qwer", "adsfg"))(_.length)
          * }}}
          */""")

      inside(Comments.parseAndRender[Mode.Exercise](comment)) {
        case Xor.Right(parsed) ⇒

          // remove XML and check that there is code content
          val description = parsed.description
            .map(_.replaceAll("\\<.*?\\>", ""))
            .getOrElse("")

          assert(
            description.trim.length > 50,
            "Issue #309: code segment was not properly rendered"
          )
      }

    }
  }

}
