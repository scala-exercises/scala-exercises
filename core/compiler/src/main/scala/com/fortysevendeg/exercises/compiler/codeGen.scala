package com.fortysevendeg.exercises
package compiler

import scala.language.implicitConversions
import scala.reflect.api.Universe

case class CodeGen[U <: Universe](override val u: U = scala.reflect.runtime.universe) extends TreeHelpers[U] {
  import u._

  def emitExercise(
    name: Option[String]
  ) = {
    val term = makeTermName("EXC", name)
    term → q"""
      object $term extends Exercise {
        override val name = $name
      }
      """
  }

  def emitSection(
    name:          String,
    description:   Option[String] = None,
    exerciseTerms: List[TermName] = Nil
  ) = {
    val term = makeTermName("SEC", name)
    term → q"""
      object $term extends Section {
        override val name         = $name
        override val description  = $description
        override val exercises    = $exerciseTerms
      }
      """
  }

  def emitLibrary(
    name:         String,
    description:  String,
    color:        String,
    sectionTerms: List[TermName] = Nil
  ) = {
    val term = makeTermName("LIB", name)
    term → q"""
      object $term extends Library {
        override val name         = $name
        override val description  = $description
        override val color        = $color
        override val sections     = $sectionTerms
      }
    """
  }

  def emitPackage(
    packageName:   String,
    libraryTree:   Tree,
    sectionTrees:  List[Tree] = Nil,
    exerciseTrees: List[Tree] = Nil
  ) = q"""
      package ${makeRefTree(packageName)} {
        import com.fortysevendeg.exercises.Exercise
        import com.fortysevendeg.exercises.Library
        import com.fortysevendeg.exercises.Section

        $libraryTree
        ..$sectionTrees
        ..$exerciseTrees
      }
    """

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
      case _            ⇒ ??? // eeh
    }
    go(path.split('.').toList.reverse)
  }

  def unblock(tree: Tree): List[Tree] = tree match {
    case Block(stats, q"()") ⇒ stats
    case Block(stats, expr)  ⇒ stats ::: expr :: Nil
    case other               ⇒ other :: Nil
  }

  case class EZTree(tree: Tree) {
    def code = showCode(tree)
    def raw = showRaw(tree)
  }

  object EZTree {
    implicit def fromTree(tree: Tree): EZTree = EZTree(tree)
  }

}

object TestApp extends App {

  /*
  val cg = CodeGen()
  import cg.EZTree._

  val s1e = List(
    cg.exercise(Some("Example1")),
    cg.exercise(Some("Example2"))
  )

  val section1 = cg.section(
    name = "Section 1",
    exercises = s1e.map(_._1)
  )

  val (libterm, lib) = cg.library(
    name = "MyLibrary",
    description = "This is my library",
    color = "#FFFFFF",
    sections = section1._1 :: Nil
  )

  val res = cg.wrap(
    pkg = "fail.sauce",
    library = lib,
    sections = section1._2 :: Nil,
    exercises = s1e.map(_._2)
  )

  println(res.code)
  */

}
