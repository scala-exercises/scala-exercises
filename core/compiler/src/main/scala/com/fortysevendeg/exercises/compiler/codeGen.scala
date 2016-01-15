package com.fortysevendeg.exercises
package compiler

import scala.language.implicitConversions
import scala.reflect.api.Universe

case class CodeGen[U <: Universe](
    override val u: U = scala.reflect.runtime.universe
) extends TreeHelpers[U] {
  import u._

  def emitExercise(name: Option[String]) = {
    val term = makeTermName("EXC", name)
    term → q"""
      object $term extends Exercise {
        override val name = $name
      }"""
  }

  def emitSection(
    name: String, description: Option[String], exerciseTerms: List[TermName]
  ) = {
    val term = makeTermName("SEC", name)
    term → q"""
      object $term extends Section {
        override val name         = $name
        override val description  = $description
        override val exercises    = $exerciseTerms
      }"""
  }

  def emitLibrary(
    name: String, description: String, color: String,
    sectionTerms: List[TermName]
  ) = {
    val term = makeTermName("LIB", name)
    term → q"""
      object $term extends Library {
        override val name         = $name
        override val description  = $description
        override val color        = $color
        override val sections     = $sectionTerms
      }"""
  }

  def emitPackage(
    packageName: String, libraryTree: Tree, sectionTrees: List[Tree],
    exerciseTrees: List[Tree]
  ) = q"""
      package ${makeRefTree(packageName)} {
        import com.fortysevendeg.exercises.Exercise
        import com.fortysevendeg.exercises.Library
        import com.fortysevendeg.exercises.Section

        $libraryTree
        ..$sectionTrees
        ..$exerciseTrees
      }"""

}

sealed trait TreeHelpers[U <: Universe] {
  val u: U
  import u._

  protected def makeTermName(appellation: String, name: String): TermName =
    makeTermName(appellation, Some(name))

  protected def makeTermName(appellation: String, name: Option[String]): TermName =
    internal.reificationSupport.freshTermName(
      appellation + name.map("_" + _.trim.replace(" ", "_")).getOrElse("")
    )

  protected def makeRefTree(path: String): RefTree = {
    def go(rem: List[String]): RefTree = rem match {
      case head :: Nil  ⇒ Ident(TermName(head))
      case head :: tail ⇒ Select(go(tail), TermName(head))
      case Nil ⇒
        // This situation should never occur, and definitely not during
        // recursion of this method. Assume we got here because path.split
        // below resulted in an empty list.
        Ident(TermName(path))
    }
    go(path.split('.').toList.reverse)
  }

  protected def unblock(tree: Tree): List[Tree] = tree match {
    case Block(stats, q"()") ⇒ stats
    case Block(stats, expr)  ⇒ stats ::: expr :: Nil
    case other               ⇒ other :: Nil
  }

}
