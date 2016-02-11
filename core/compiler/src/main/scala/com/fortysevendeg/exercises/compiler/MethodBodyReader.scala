package com.fortysevendeg.exercises
package compiler

import scala.tools.nsc.Global

object MethodBodyReader {

  /** TODO: This doesn't work very well!
    * Need to figure out a better way to get a blob of code for a given method!
    */
  def read[G <: Global](g: G)(expr: g.Tree): String = {
    import g._

    def range(pos: Position): Option[(Int, Int)] = pos match {
      case `NoPosition` ⇒ None
      case _            ⇒ Some(pos.start → pos.end)
    }

    def skipToEol(str: String, start: Int): Int =
      if (start < str.length && (str charAt start) != '\n') skipToEol(str, start + 1)
      else start

    def loop(trees: List[Tree], start: Int, end: Int): (Int, Int) = trees match {
      case Nil ⇒ (start, end)
      case tree :: rs ⇒
        val (start0, end0) = range(tree.pos)
          .map(pos ⇒ math.min(start, pos._1) →
            math.max(end, pos._2))
          .getOrElse(start → end)

        loop(tree.children ::: rs, start0, end0)
    }

    range(expr.pos) match {
      case None ⇒ "<<unavailable>>"
      case Some((start, end)) ⇒
        // this is the entire blob of source code
        val content = expr.pos.source.content.mkString

        // this is roughly the start/end of the block we want to extract
        // Can't seem to capture the trailing expression in this range
        val (start0, end0) = loop(expr :: Nil, start, end)

        // uuuh... this attempts to catch the trailing item... but fails
        // miserably in cases where the last line has lots of unrelated stuff
        // on the end!
        val end1 = skipToEol(content, end0)
        content.slice(start0, end1)
    }

  }

}
