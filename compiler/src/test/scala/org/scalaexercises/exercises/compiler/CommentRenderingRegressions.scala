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

import cats.implicits._

class CommentRenderingRegressions extends FunSpec with Matchers with Inside {
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
        case Right(parsed) ⇒
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

    it("github scala-exercises/exercises-stdlib#50") {
      val comment = commentFactory.parse(s"""
        /**
         * {{{
         * abstract class Soldier(val firstName: String, val lastName: String) {}
         *
         * // if you uncomment this line, it will fail compilation
         * //val soldier = new Soldier
         * }}}
         */""")

      inside(Comments.parseAndRender[Mode.Exercise](comment)) {
        case Right(parsed) ⇒
          val description = parsed.description
            .map(_.replaceAll("\\<.*?\\>", ""))
            .getOrElse("")

          assert(
            description.trim ==
              """abstract class Soldier(val firstName: String, val lastName: String) {}
                |
                |// if you uncomment this line, it will fail compilation
                |//val soldier = new Soldier""".stripMargin,
            "Issue scala-exercises/exercises-stdlib#50: comment rendering problem"
          )
      }
    }
  }

}
