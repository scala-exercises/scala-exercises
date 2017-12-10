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

import scala.annotation.tailrec

import scala.reflect.internal.Chars.isWhitespace
import scala.tools.nsc.Global

object MethodBodyReader {

  /** Attempts to read (and clean) a method body.
   */
  def read[G <: Global](g: G)(tree: g.Tree): String = {
    val (bodyStart, bodyEnd) = bodyRange(g)(tree)

    val content    = tree.pos.source.content
    val lineRanges = normalizedLineRanges(content, bodyStart, bodyEnd)

    lineRanges
      .map { lineRange ⇒
        content.slice(lineRange._1, lineRange._2).mkString
      }
      .mkString("\n")
  }

  /** Finds the text range for the body of the method.
   * This should:
   * - ignore the wrapping block brackets
   * - include any leading whitespace before the first expression
   * in multi line statements
   */
  def bodyRange[G <: Global](g: G)(tree: g.Tree): (Int, Int) = {
    import g._
    tree match {
      case Block(stats, expr) ⇒
        val firstTree = if (stats.nonEmpty) stats.head else expr
        val lastTree  = expr
        val start     = firstTree.pos.start
        val end       = lastTree.pos.end
        val start0 = backstepWhitespace(
          tree.pos.source.content,
          tree.pos.start,
          start
        )
        (start0, end)
      case _ ⇒
        val source      = tree.pos.source
        val startOffset = source.lineToOffset(source.offsetToLine(tree.pos.start))
        val endOffset   = endOfLineOffset(g)(tree, tree.pos.end)
        (startOffset, endOffset)
    }
  }

  @tailrec private def backstepWhitespace(str: Array[Char], start: Int, end: Int): Int = {
    if (end > start && isWhitespace(str(end - 1)))
      backstepWhitespace(str, start, end - 1)
    else end
  }

  @tailrec private def endOfLineOffset[G <: Global](g: G)(tree: g.Tree, endOffset: Int): Int = {
    if (endOffset < tree.pos.source.length && !tree.pos.source.isEndOfLine(endOffset))
      endOfLineOffset(g)(tree, endOffset + 1)
    else endOffset
  }

  /** This attempts to find all the individual lines in a method body
   * while also counting the amount of common prefix whitespace on each line.
   */
  private def normalizedLineRanges(str: Array[Char], start: Int, end: Int): List[(Int, Int)] = {

    @tailrec def skipToEol(offset: Int): Int =
      if (offset < end && str(offset) != '\n') skipToEol(offset + 1)
      else offset

    @tailrec def skipWhitespace(offset: Int): Int =
      if (offset < end && isWhitespace(str(offset))) skipWhitespace(offset + 1)
      else offset

    type Acc = List[(Int, Int)]
    @tailrec def loop(i: Int, minSpace: Int, acc: Acc): (Int, Acc) = {
      if (i >= end) minSpace → acc
      else {
        val lineStart = skipWhitespace(i)
        val lineEnd   = skipToEol(lineStart)

        if (lineStart == lineEnd)
          loop(
            lineEnd + 1,
            minSpace,
            (i, i) :: acc
          )
        else
          loop(
            lineEnd + 1,
            math.min(lineStart - i, minSpace),
            (i, lineEnd) :: acc
          )
      }
    }

    val (minSpace, offsets) = loop(start, Int.MaxValue, Nil)
    offsets.map { kv ⇒
      (kv._1 + minSpace) → kv._2
    }.reverse
  }

}
