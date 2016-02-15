/*
 * scala-exercises-exercise-compiler
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises
package compiler

import scala.reflect.api.Universe

/** This is responsible for generating exercise code. It generates
  * scala compiler trees, which can be evaluated or rendered to
  * source code.
  */
case class TreeGen[U <: Universe](
    u: U = scala.reflect.runtime.universe
) {
  import u._

  def makeExercise(
    name: Option[String], description: Option[String],
    code: Option[String], qualifiedMethod: Option[String],
    explanation: Option[String]
  ) = {
    val term = makeTermName("Exercise", name)
    term → q"""
        object $term extends Exercise {
          override val name             = $name
          override val description      = $description
          override val code             = $code
          override val qualifiedMethod  = $qualifiedMethod
          override val explanation      = $explanation
        }"""
  }

  def makeSection(
    name: String, description: Option[String],
    exerciseTerms: List[TermName]
  ) = {
    val term = makeTermName("Section", name)
    term → q"""
        object $term extends Section {
          override val name         = $name
          override val description  = $description
          override val exercises    = $exerciseTerms
        }"""
  }

  def makeLibrary(
    name: String, description: String, color: Option[String],
    sectionTerms: List[TermName]
  ) = {
    val term = makeTermName("Library", name)
    term → q"""
        object $term extends Library {
          override val name         = $name
          override val description  = $description
          override val color        = $color
          override val sections     = $sectionTerms
        }"""
  }

  def makePackage(
    packageName: String,
    trees:       List[Tree]
  ) = q"""
        package ${makeRefTree(packageName)} {
          import com.fortysevendeg.exercises.Exercise
          import com.fortysevendeg.exercises.Library
          import com.fortysevendeg.exercises.Section
          ..$trees
        }"""

  private def makeTermName(appellation: String, name: String): TermName =
    makeTermName(appellation, Some(name))

  private def makeTermName(appellation: String, name: Option[String]): TermName = {
    val nameSanSymbols = name.map(_.trim.replaceAll("[^0-9a-zA-Z]+", ""))
    internal.reificationSupport.freshTermName(
      appellation + nameSanSymbols.map("_" + _ + "$").getOrElse("")
    )
  }

  private def makeRefTree(path: String): RefTree = {
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

}
