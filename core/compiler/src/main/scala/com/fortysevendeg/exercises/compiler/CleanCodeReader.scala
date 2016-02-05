package com.fortysevendeg.exercises
package compiler

import scala.tools.nsc._

object CleanCodeReader {

  /** Reads a chunk of code associated with the body of a def tree.
    * Additionally, performs some cleanup on the code. This cleanup might be
    * _FICKLE_, so watch out! Try to remove any reasonable whitespace
    * and any excess indentation.
    */
  def read[G <: Global](g: G)(expr: g.Tree): String = {
    import g._

    implicit class TreeOps(tree: Tree) {
      /** The raw code string for a tree */
      def code = tree.pos.source.content.mkString
    }

    expr match {
      // no code, easy
      case Literal(Constant(())) ⇒ ""
      // a block of multiple expressions, with a possible retval
      case Block(stats, retExpr) ⇒

        val start = if (stats.nonEmpty) stats.head.pos.start else retExpr.pos.start
        val end = retExpr match {
          // If there is no return value, then whitespace up till the block's
          // closing bracket will be captured in retExpr.
          // Don't use it for positions.
          case Literal(Constant(())) ⇒ stats.last.pos.end
          case _                     ⇒ retExpr.pos.end
        }

        val code = expr.code

        // look at the whitespace in the last line between the opening bracket
        // and the first statement.
        val line0 = code.slice(expr.pos.start, start)
        val line0padLen = line0.lines.toList match {
          case one :: Nil ⇒ 0
          case many       ⇒ many.last.length
        }

        // Look at whitespace in the statement lines
        val lines = code.slice(start, end).lines.toList
        val minPadLen = lines.tail.foldLeft(line0padLen) { (b, a) ⇒
          val checkRegion = a.take(b)
          val res = checkRegion.takeWhile(_ == ' ').length
          // ignore lines that are empty (just whitespace)
          if (res != a.length) res else b
        }

        (lines.head :: lines.tail.map(_.drop(minPadLen))).mkString("\n")

      // a single expression
      case _ ⇒ expr.code.slice(expr.pos.start, expr.pos.end)
    }
  }

}
