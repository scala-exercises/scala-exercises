package com.fortysevendeg.exercises
package compiler

import scala.tools.nsc._

object MethodBodyReader {

  /** TODO: This doesn't work for all cases!
    * Need to figure out a better way to get a blob of code for a given method!
    */
  def read[G <: Global](g: G)(expr: g.Tree): String = {
    import g._

    def skipToEol(str: String, start: Int): Int =
      if (start < str.length && (str charAt start) != '\n') skipToEol(str, start + 1)
      else start

    def loop(trees: List[Tree], start: Int, end: Int): (Int, Int) = trees match {
      case Nil ⇒ (start, end)
      case tree :: rs ⇒
        loop(
          tree.children ::: rs,
          math.min(start, tree.pos.start),
          math.max(end, tree.pos.end)
        )
    }

    // this is the entire blob of source code
    val content = expr.pos.source.content.mkString

    // this is roughly the start/end of the block we want to extract
    // Can't seem to capture the trailing expression in this range
    val (start, end) = loop(expr :: Nil, expr.pos.start, expr.pos.end)

    // uuuh... this attempts to catch the trailing item... but fails
    // miserably in cases where the last line has lots of unrelated stuff
    // on the end!
    val end2 = skipToEol(content, end)
    content.slice(start, end2)

  }

}
