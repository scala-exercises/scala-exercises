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
import scala.language.postfixOps

import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc._
import scala.tools.nsc.doc.{Settings ⇒ _, _}

import scala.tools.nsc.doc.base.CommentFactoryBase
import scala.tools.nsc.doc.base.MemberLookupBase
import scala.tools.nsc.doc.base.LinkTo
import scala.tools.nsc.doc.base.LinkToExternal
import scala.tools.nsc.doc.base.comment.Comment

class SourceTextExtraction {
  lazy val global                  = new DocExtractionGlobal()
  private lazy val commentFactory  = CommentFactory(global)
  private lazy val boundExtractRaw = SourceTextExtraction.extractRaw(global)(_)
  private lazy val boundReadCode   = MethodBodyReader.read(global)(_)

  import global._

  class ExtractedMethod private[SourceTextExtraction] (
      lazyCode: ⇒ String,
      lazyImports: ⇒ List[String]
  ) {
    lazy val code    = lazyCode
    lazy val imports = lazyImports
  }

  class ExtractedComment private[SourceTextExtraction] (
      lazyRaw: ⇒ String,
      lazyComment: ⇒ Comment
  ) {
    lazy val raw     = lazyRaw
    lazy val comment = lazyComment
  }

  case class Extracted(
      symbolPaths: Map[String, String],
      comments: Map[List[String], ExtractedComment],
      methods: Map[List[String], ExtractedMethod]
  )

  def relativePath(absolutePath: String, base: String) =
    absolutePath.split(base).lift(1).getOrElse("")

  def extractAll(sources: List[String], paths: List[String], baseDir: String): Extracted = {
    new global.Run() compileSources (paths zip sources).map({
      case (path, code) ⇒ new BatchSourceFile(path, code)
    })
    val run = global.currentRun
    val symbolPaths = Map(run.symSource.toList: _*).map({
      case (symbol, file) ⇒ (symbol.toString, relativePath(file.path, baseDir))
    })
    val compilationUnits = run.units.toList // `units` is only only iterable once!
    val extractions      = compilationUnits.map(_.body).map(boundExtractRaw)

    def nameToString(name: Name) = name match {
      case TermName(value) ⇒ value
      case TypeName(value) ⇒ value
    }

    def expandPath[T](kv: (List[global.Name], T)): (List[String], T) =
      (kv._1.map(nameToString), kv._2)

    def splitPackage(p: List[String]) =
      p flatMap (_.split('.').toList)

    val (commentss, methodss) = extractions.map { extraction ⇒
      val comments = extraction.comments.map(expandPath).map {
        case (k, v) ⇒
          splitPackage(k) → new ExtractedComment(v._2.raw, commentFactory.parse(v._2))
      }

      val rawImports = extraction.imports
      val paths      = rawImports.map(expandPath)

      val imports = paths
        .groupBy(_._1)
        .mapValues(_.map(_._2))

      val methods = extraction.methods.map(expandPath).map {
        case (k, v) ⇒
          lazy val methodImports = k
            .scanLeft(Nil: List[String])((a, c) ⇒ c :: a)
            .map(_.reverse)
            .flatMap(imports.get)
            .flatten
            .collect {
              case (order, imp) if order < v._1 ⇒ showCode(imp)
            }
          splitPackage(k) → new ExtractedMethod(boundReadCode(v._2), methodImports)
      }
      (comments, methods)
    }.unzip

    Extracted(
      symbolPaths,
      commentss.flatten.toMap,
      methodss.flatten.toMap
    )

  }

}

/** Utility to find doc exercise-worthy comments and source code blobs
 * in a tree.
 */
object SourceTextExtraction {

  type Path[G <: Global] = List[G#Name]
  case class RawAcc[G <: Global](
      comments: List[(Path[G], (Int, G#DocComment))] = Nil,
      methods: List[(Path[G], (Int, G#Tree))] = Nil,
      imports: List[(Path[G], (Int, G#Import))] = Nil,
      position: Int = 0
  )

  def extractRaw[G <: Global](g: G)(rootTree: g.Tree): RawAcc[g.type] = {
    import g._

    /** Define generic accumulating traversal that visits all the nodes of
     * interest.
     */
    def traverse[A](
        trees0: List[(Path[g.type], Tree)],
        acc0: A,
        visitDocComment: (Path[g.type], g.DocComment, A) ⇒ A,
        visitMethodExpr: (Path[g.type], g.Tree, A) ⇒ A,
        visitImport: (Path[g.type], g.Import, A) ⇒ A
    ): A = {

      // a nested function so that we don't have to include visitDocComment and
      // visitMethodExpr as trailing params on each recursive call
      @tailrec def traversal(trees: List[(Path[g.type], Int, Tree)], acc: A): A = trees match {
        case Nil ⇒ acc
        case (path, order, tree) :: rs ⇒
          tree match {

            case DocDef(comment, moduleDef @ ModuleDef(mods, _, impl)) ⇒
              val nextPath = moduleDef.name :: path
              traversal(
                impl.body.zipWithIndex.map { case (body, index) ⇒ (nextPath, index, body) } ::: rs,
                visitDocComment(nextPath.reverse, comment, acc)
              )

            // TODO: is this needed?
            case DocDef(comment, classDef @ ClassDef(mods, _, Nil, impl)) ⇒
              val nextPath = classDef.name :: path
              traversal(
                impl.body.zipWithIndex.map { case (body, index) ⇒ (nextPath, index, body) } ::: rs,
                visitDocComment(nextPath.reverse, comment, acc)
              )

            case DocDef(comment, q"def $tname(...$paramss): $tpt = $expr") ⇒
              val nextPath         = tname :: path
              val nextPathReversed = nextPath.reverse
              traversal(
                rs,
                visitMethodExpr(
                  nextPathReversed,
                  expr,
                  visitDocComment(nextPathReversed, comment, acc))
              )

            case moduleDef @ ModuleDef(mods, _, impl) ⇒
              val nextPath = moduleDef.name :: path
              traversal(
                impl.body.zipWithIndex.map { case (body, index) ⇒ (nextPath, index, body) } ::: rs,
                acc
              )

            // TODO: is this needed?
            case classDef @ ClassDef(mods, _, Nil, impl) ⇒
              val nextPath = classDef.name :: path
              traversal(
                impl.body.zipWithIndex.map { case (body, index) ⇒ (nextPath, index, body) } ::: rs,
                acc
              )

            /*
          // TODO: can this be removed?
          case q"def $tname(...$paramss): $tpt = $expr" ⇒
            val nextPath = tname :: path
            traversal(
              (nextPath, 0, expr) :: rs,
              acc
            )
             */

            case q"package $ref { ..$topstats }" ⇒
              val nextPath =
                if (ref.name == termNames.EMPTY_PACKAGE_NAME) path
                else TermName(ref.toString) :: path
              traversal(
                topstats.zipWithIndex.map { case (body, index) ⇒ (nextPath, index, body) } ::: rs,
                acc
              )

            case imp: g.Import ⇒
              traversal(
                rs,
                visitImport(path.reverse, imp, acc)
              )

            case _ ⇒
              traversal(
                rs,
                acc
              )
          }
      }
      // go
      traversal(trees0.map(kv ⇒ (kv._1, 0, kv._2)), acc0)
    }

    traverse[RawAcc[g.type]](
      trees0 = List(Nil → rootTree),
      acc0 = RawAcc[g.type](Nil, Nil),
      visitDocComment = { (path, comment, acc) ⇒
        acc.copy(
          comments = (path, (acc.position, comment)) :: acc.comments,
          position = acc.position + 1
        )
      //RawAcc(comments = (path → comment) :: Nil)
      },
      visitMethodExpr = { (path, expr, acc) ⇒
        acc.copy(
          methods = (path, (acc.position, expr)) :: acc.methods,
          position = acc.position + 1
        )
      //RawAcc(methods = (path → expr) :: Nil)
      },
      visitImport = { (path, imp, acc) ⇒
        val newElement: List[(Path[g.type], (RunId, g.Import))] = List((path, (acc.position, imp)))
        acc.copy(
          imports = acc.imports ++ newElement,
          position = acc.position + 1
        )
      }
    )
  }

}

/** Scala compiler global needed for extracting doc comments. This uses the
 * ScaladocSyntaxAnalyzer, which keeps DocDefs in the parsed AST.
 *
 * It would be ideal to do this as a compiler plugin. Unfortunately there
 * doesn't seem to be a way to replace the syntax analyzer phase (named
 * "parser") with a plugin.
 */
class DocExtractionGlobal(settings: Settings = DocExtractionGlobal.defaultSettings)
    extends Global(settings) {

  override lazy val syntaxAnalyzer = new ScaladocSyntaxAnalyzer[this.type](this) {
    val runsAfter        = List[String]()
    val runsRightAfter   = None
    override val initial = true
  }

  override def newUnitParser(unit: CompilationUnit) =
    new syntaxAnalyzer.ScaladocUnitParser(unit, Nil)

  override protected def computeInternalPhases() {
    phasesSet += syntaxAnalyzer
    phasesSet += analyzer.namerFactory
  }

}

object DocExtractionGlobal {
  def defaultSettings = new Settings {
    embeddedDefaults[DocExtractionGlobal.type]
    // this flag is crucial for method body extraction
    Yrangepos.value = true
  }
}
