package com.fortysevendeg.exercises
package compiler

import cats._
import cats.std.all._
import cats.syntax.flatMap._

import scala.reflect.internal.util.FakePos
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.ast.parser.SyntaxAnalyzer
import scala.tools.nsc.doc.Settings
import scala.tools.nsc.doc.ScaladocGlobal
import scala.tools.nsc.interactive.Response
import scala.tools.nsc.doc.base.CommentFactoryBase
import scala.tools.nsc.doc.base.LinkTo
import scala.tools.nsc.doc.base.LinkToExternal
import scala.tools.nsc.doc.base.MemberLookupBase
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.Reporter

/** Global for compiling exercises. We piggyback on the `ScalaDocGlobal`,
  * and let it do most of the heavy lifting. Otherwise it'd be a lot more work
  * to get parsed `DocDef`s (Scaladoc comments).
  */
private[compiler] class ScalaExerciseGlobal(
  settings: Settings,
  reporter: Reporter
) extends ScaladocGlobal(settings, reporter)
    with MemberLookupBase with CommentFactoryBase {

  locally { new Run() }

  // begin MemberLookup/CommentFactory related
  // TODO?-- implement these to provide links between exercies and categories
  override val global = this
  override def internalLink(sym: global.Symbol, site: global.Symbol): Option[LinkTo] = None
  override def chooseLink(links: List[LinkTo]): LinkTo = links.head
  override def toString(link: LinkTo) = "<<not supported>>"
  override def findExternalLink(sym: global.Symbol, name: String): Option[LinkToExternal] = None
  override def warnNoLink = false
  // end MemberLookup/CommentFactory related

  def parseComment(comment: DocComment) = parseAtSymbol(comment.raw, comment.raw, comment.pos)

}

private[compiler] class ScalaExerciseSettings extends Settings(println, println) {
  // To my knowledge... this instructs the compile to use defaults
  // provided by the current classloader (classloader of this.type).
  // Without this, received 'missing `scala` package' errors when
  // launching the compiler.
  embeddedDefaults[this.type]
}

object ScalaExerciseParser {
  type Doc = scala.tools.nsc.doc.base.comment.Comment

  case class Category(
    doc:       Option[Doc],
    className: String,
    exercises: List[Exercise]
  )

  case class Exercise(
    doc:  Option[Doc],
    code: Option[String]
  )
}

case class ScalaExerciseParser() {
  import ScalaExerciseParser._

  private val settings = new ScalaExerciseSettings()
  private val reporter = new ConsoleReporter(settings)
  private[compiler] val compiler = new ScalaExerciseGlobal(settings, reporter)
  import compiler._

  def parse(code: String) = {
    val scanner = compiler.newUnitParser(code)
    val tree = scanner.compilationUnit()
    traverseRootTree(tree)
  }

  private[compiler] def traverseExerciseBodyTree(tree: Tree): List[Exercise] = tree match {

    case DocDef(comment, q"def $tname(...$paramss): $tpt = $expr") ⇒
      Exercise(
        doc = Some(compiler.parseComment(comment)),
        code = Some(readCleanExpr(expr))
      ) :: Nil

    case _ ⇒ Nil
  }

  /** Traverse top level tree statements */
  private[compiler] def traverseRootTree(tree: Tree): List[Category] = tree match {

    // Dive into packages
    case q"package $ref { ..$topstats }" ⇒ topstats >>= traverseRootTree

    // An exercise preceeded by header doc comments
    case DocDef(comment, ClassDef(mods, name, Nil, impl)) ⇒

      Category(
        doc = Some(compiler.parseComment(comment)),
        className = name.toString(),
        exercises = impl.body >>= traverseExerciseBodyTree
      ) :: Nil

    // An exercise without any header comments
    case ClassDef(mods, name, Nil, impl) ⇒

      Category(
        doc = None,
        className = name.toString(),
        exercises = impl.body >>= traverseExerciseBodyTree
      ) :: Nil

    // Unhandled items
    case tree: Tree ⇒
      Nil
  }

  private implicit class TreeOps(tree: Tree) {
    /** The raw code string for a tree */
    def code = tree.pos.source.content.mkString
  }

  /** Reads a chunk of code associated with the body of a def tree.
    * Additionally, performs some cleanup on the code. This cleanup might be
    * _FICKLE_, so watch out! Try to remove any reasonable whitespace
    * and any excess indentation.
    */
  private[compiler] def readCleanExpr(expr: Tree): String = expr match {
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
