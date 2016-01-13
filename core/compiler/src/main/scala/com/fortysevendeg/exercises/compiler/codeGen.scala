package com.fortysevendeg.exercises
package compiler

import scala.language.implicitConversions
import scala.reflect.api.Universe

case class CodeGen(universe: Universe = scala.reflect.runtime.universe) extends TreeHelpers {
  import universe._

  def exercise(
    name: Option[String]
  ) = {
    val term = makeTermName("EXC", name)
    term → q"""
      object $term extends Exercise {
        override val name = $name
      }
      """
  }

  def section(
    name:        String,
    description: Option[String] = None,
    exercises:   List[TermName] = Nil
  ) = {
    val term = makeTermName("SEC", name)
    term → q"""
      object $term extends Section {
        override val name         = $name
        override val description  = $description
        override val exercises    = $exercises
      }
      """
  }

  def library(
    name:        String,
    description: String,
    color:       String,
    sections:    List[TermName] = Nil
  ) = q"""
      object ${makeTermName("LIB", name)} extends Library {
        override val name         = $name
        override val description  = $description
        override val color        = $color
        override val sections     = $sections
      }
    """

  def wrap(
    pkg:       String,
    library:   Tree,
    sections:  List[Tree],
    exercises: List[Tree]
  ) = q"""
      package ${makeRefTree(pkg)} {
        import com.fortysevendeg.exercises.Exercise
        import com.fortysevendeg.exercises.Library
        import com.fortysevendeg.exercises.Section

        $library
        ..$sections
        ..$exercises
      }
    """

}

sealed trait TreeHelpers {
  val universe: Universe
  import universe._

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

  val lib = cg.library(
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

}
