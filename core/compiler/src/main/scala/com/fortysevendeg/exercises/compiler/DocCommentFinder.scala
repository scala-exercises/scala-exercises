package com.fortysevendeg.exercises
package compiler

import scala.annotation.tailrec
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.Global

class DocCommentFinder {
  private lazy val docGlobal = new DocExtractionGlobal()
  private lazy val docGlobalFindAllRaw = DocCommentFinder.findAllRaw(docGlobal)(_)

  import docGlobal._

  def findAll(sources: List[String]): Map[List[String], String] = {
    new docGlobal.Run() compileSources sources.map(code ⇒ new BatchSourceFile("(internal)", code))
    docGlobal.currentRun.units.map(_.body).flatMap(docGlobalFindAllRaw)
      .map {
        case (k, v) ⇒ k.collect {
          case TermName(value) ⇒ value
          case TypeName(value) ⇒ value
        } → v.raw
      }
      .toMap
  }
}

/** Utility to find doc comments in a tree. */
object DocCommentFinder {

  type Path[G <: Global] = List[G#Name]
  type Acc[G <: Global] = List[(Path[G], G#DocComment)]

  def findAllRaw[G <: Global](g: G)(rootTree: g.Tree): Acc[g.type] = {
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
          val nextPath =
            if (ref.name == termNames.EMPTY_PACKAGE_NAME) path
            else ref.name :: path
          traverseAcc(topstats.map(nextPath → _) ::: rs, acc)

        case _ ⇒
          traverseAcc(rs, acc)
      }
    }
    traverseAcc(List(Nil → rootTree), Nil)
  }

}
