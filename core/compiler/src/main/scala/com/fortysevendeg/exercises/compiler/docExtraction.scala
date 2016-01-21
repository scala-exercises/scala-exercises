package com.fortysevendeg.exercises
package compiler

import scala.annotation.tailrec

import cats._
import cats.std.all._
import cats.syntax.flatMap._

import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc._
import scala.tools.nsc.doc.{ Settings ⇒ _, _ }
import scala.tools.nsc.plugins._
import scala.util.Try

class DocExtractionGlobal(settings: Settings = DocExtractionGlobal.defaultSettings) extends Global(settings) {

  override lazy val syntaxAnalyzer = new ScaladocSyntaxAnalyzer[this.type](this) {
    val runsAfter = List[String]()
    val runsRightAfter = None
    override val initial = true
  }

  override def newUnitParser(unit: CompilationUnit) = new syntaxAnalyzer.ScaladocUnitParser(unit, Nil)

  override protected def computeInternalPhases() {
    phasesSet += syntaxAnalyzer
    phasesSet += analyzer.namerFactory
  }

}

object DocExtractionGlobal {

  def defaultSettings = new Settings {
    embeddedDefaults[DocExtractionGlobal.type]
  }

  class Java extends DocExtractionGlobal() {

    import scala.collection.JavaConverters._
    import java.{ util ⇒ ju }

    private val findByName = DocCommentFinder.findByName(this) _

    def findAll(code: String): ju.Map[String, String] = {
      new Run() compileSources List(
        new BatchSourceFile("hot-sauce", code)
      )
      val rootTree = currentRun.units.toList.head.body
      val res = DocCommentFinder.findAll(this)(rootTree)
        .map { case (k, v) ⇒ k.mkString(".") → v.raw }
        .toMap
      res.asJava
    }

    // TODO: potentially remove this, since it probably isnt' needed
    def find(code: String, symbols: Array[String]): Array[String] = {
      new Run() compileSources List(
        new BatchSourceFile("hot-sauce", code)
      )
      val rootTree = currentRun.units.toList.head.body
      val res = findByName(rootTree, symbols)
        .mapValues(_.raw)
      symbols.map(name ⇒ res.get(name).getOrElse(null))
    }
  }
}

object DocCommentFinder {

  type Path[G <: Global] = List[G#Name]
  type Acc[G <: Global] = List[(Path[G], G#DocComment)]

  def findAll[G <: Global](g: G)(rootTree: g.Tree): Acc[g.type] = {

    import g._

    @tailrec def traverseAcc(trees: List[(Path[g.type], Tree)], acc: Acc[g.type]): Acc[g.type] = trees match {
      case Nil ⇒ acc
      case (path, tree) :: rs ⇒ tree match {

        case DocDef(comment, moduleDef @ ModuleDef(mods, _, impl)) ⇒
          val nextPath = moduleDef.name :: path
          traverseAcc(impl.body.map(nextPath → _) ::: rs, (nextPath.reverse → comment) :: acc)

        case DocDef(comment, classDef @ ClassDef(mods, _, Nil, impl)) ⇒
          val nextPath = classDef.name :: path
          traverseAcc(impl.body.map(nextPath → _) ::: rs, (nextPath.reverse → comment) :: acc)

        case DocDef(comment, q"def $tname(...$paramss): $tpt = $expr") ⇒
          val nextPath = tname :: path
          traverseAcc(rs, (nextPath.reverse → comment) :: acc)

        case moduleDef @ ModuleDef(mods, _, impl) ⇒
          val nextPath = moduleDef.name :: path
          traverseAcc(impl.body.map(nextPath → _) ::: rs, acc)

        case classDef @ ClassDef(mods, _, Nil, impl) ⇒
          val nextPath = classDef.name :: path
          traverseAcc(impl.body.map(nextPath → _) ::: rs, acc)

        case q"def $tname(...$paramss): $tpt = $expr" ⇒
          val nextPath = tname :: path
          traverseAcc((nextPath → expr) :: rs, acc)

        case q"package $ref { ..$topstats }" ⇒
          val nextPath = ref.name :: path
          traverseAcc(topstats.map(nextPath → _) ::: rs, acc)

        case _ ⇒
          traverseAcc(rs, acc)
      }
    }
    traverseAcc(List(Nil → rootTree), Nil)
  }

  // TODO: potentially remove this, since it probably isnt' needed
  def findByName[G <: Global](g: G)(rootTree: g.Tree, searchNames: Iterable[String]): Map[String, g.DocComment] = {
    import g.{ Try ⇒ _, _ }
    val searchSymbolsMap: Map[Symbol, String] = searchNames.flatMap(name ⇒ Try(g.rootMirror.staticModule(name) → name).toOption)(collection.breakOut)
    findBySymbol(g)(rootTree, searchSymbolsMap.keys)
      .map(kv ⇒ searchSymbolsMap(kv._1) → kv._2)
  }

  // TODO: potentially remove this, since it probably isnt' needed
  def findBySymbol[G <: Global](g: G)(rootTree: g.Tree, searchSymbols: Iterable[g.Symbol]): Map[g.Symbol, g.DocComment] = {
    import g._
    class DocCommentSearchTraverser(searchSymbols: Iterable[Symbol], root: Tree) extends Traverser {
      val remainingItems = collection.mutable.HashSet[Symbol]() ++ searchSymbols
      val found = collection.mutable.HashMap[Symbol, DocComment]()
      def done = false // remainingItems.isEmpty
      traverse(root)
      override def traverse(tree: Tree) {
        if (!done) {
          tree match {
            case DocDef(comment, q"def $tname(...$paramss): $tpt = $expr") ⇒
            //

            case DocDef(comment, subTree) if remainingItems.contains(subTree.symbol) ⇒
              remainingItems -= subTree.symbol
              found += subTree.symbol → comment
              super.traverse(subTree)

            case _ ⇒
              super.traverse(tree)
          }
        }
      }
    }
    (new DocCommentSearchTraverser(searchSymbols, rootTree)).found.toMap
  }

}

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
