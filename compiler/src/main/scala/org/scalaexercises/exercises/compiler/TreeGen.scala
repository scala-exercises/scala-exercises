/*
 * scala-exercises-exercise-compiler
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.compiler

import scala.reflect.api.Universe

import org.scalaexercises.compiler.formatting._

/** This is responsible for generating exercise code. It generates
  * scala compiler trees, which can be evaluated or rendered to
  * source code.
  */
case class TreeGen[U <: Universe](
    u: U = scala.reflect.runtime.universe
) {
  import u._

  def makeExercise(
    libraryName:     String,
    name:            String,
    description:     Option[String],
    code:            String,
    qualifiedMethod: String,
    imports:         List[String],
    explanation:     Option[String],
    packageName:     String
  ) = {
    val term = makeTermName(s"Exercise_${libraryName}_", name)
    term → q"""
        object $term extends Exercise {
          override val name             = $name
          override val description      = $description
          override val code             = ${formatCode(code)}
          override val packageName      = $packageName
          override val qualifiedMethod  = $qualifiedMethod
          override val imports          = $imports
          override val explanation      = $explanation
        }"""
  }

  def makeContribution(
    sha:       String,
    message:   String,
    timestamp: String,
    url:       String,
    author:    String,
    authorUrl: String,
    avatarUrl: String
  ) = {
    val term = makeTermName("Contribution", sha)
    term → q"""
    object $term extends Contribution {
      override def sha = $sha
      override def message = $message
      override def timestamp = $timestamp
      override def url = $url
      override def author = $author
      override def authorUrl = $authorUrl
      override def avatarUrl = $avatarUrl
    }
    """
  }

  def makeSection(
    libraryName:       String,
    name:              String,
    description:       Option[String],
    exerciseTerms:     List[TermName],
    imports:           List[String],
    path:              Option[String] = None,
    contributionTerms: List[TermName]
  ) = {
    val term = makeTermName(s"Section_${libraryName}_", name)
    term → q"""
        object $term extends Section {
          override val name         = $name
          override val description  = $description
          override val exercises    = $exerciseTerms
          override val imports      = $imports
          override val path         = $path
          override val contributions = $contributionTerms
        }"""
  }

  def makeLibrary(
    name:         String,
    description:  String,
    color:        Option[String],
    sectionTerms: List[TermName],
    owner:        String,
    repository:   String,
    timestamp:    String
  ) = {
    val term = makeTermName("Library", name)
    term → q"""
        object $term extends Library {
          override val name         = $name
          override val description  = $description
          override val color        = $color
          override val sections     = $sectionTerms
          override val owner        = $owner
          override val repository   = $repository
          override val timestamp    = $timestamp
        }"""
  }

  def makePackage(
    packageName: String,
    trees:       List[Tree]
  ) = q"""
        package ${makeRefTree(packageName)} {
          import org.scalaexercises.runtime.model.{ Exercise, Library, Section, Contribution }
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
