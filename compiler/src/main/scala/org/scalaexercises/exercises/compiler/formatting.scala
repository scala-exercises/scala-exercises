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

import scalariform.formatter.ScalaFormatter
import cats.implicits._

object formatting {
  private[this] def wrap(code: String): String = s"""// format: OFF
      |object Wrapper {
      |  // format: ON
      |  $code
      |  // format: OFF
      |}""".stripMargin

  private[this] def unwrap(code: String): String =
    code
      .split("\n")
      .drop(3)
      .dropRight(2)
      .collect {
        case line if line.startsWith("  ") => line.drop(2)
        case line                          => line
      }
      .mkString("\n")

  def formatCode(code: String): String = {
    Either.catchNonFatal(ScalaFormatter.format(wrap(code))) match {
      case Right(result) ⇒ unwrap(result)
      case _             ⇒ code
    }
  }
}
