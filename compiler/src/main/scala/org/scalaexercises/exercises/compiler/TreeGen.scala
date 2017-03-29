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
      libraryName: String,
      name: String,
      description: Option[String],
      code: String,
      qualifiedMethod: String,
      imports: List[String],
      explanation: Option[String],
      packageName: String
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
      sha: String,
      message: String,
      timestamp: String,
      url: String,
      author: String,
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
      libraryName: String,
      name: String,
      description: Option[String],
      exerciseTerms: List[TermName],
      imports: List[String],
      path: Option[String] = None,
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

  def makeBuildInfo(
      name: String,
      resolvers: List[String],
      libraryDependencies: List[String]
  ) = {
    val term = makeTermName("BuildInfo", name)
    term → q"""
        object $term extends BuildInfo {
          override val resolvers            = $resolvers
          override val libraryDependencies  = $libraryDependencies
        }"""
  }

  def makeLibrary(
      name: String,
      description: String,
      color: Option[String],
      logoPath: String,
      logoData: Option[String],
      sectionTerms: List[TermName],
      owner: String,
      repository: String,
      timestamp: String,
      buildInfoT: TermName
  ) = {
    val term = makeTermName("Library", name)
    term → q"""
        object $term extends Library {
          override val name           = $name
          override val description    = $description
          override val color          = $color
          override val logoPath       = $logoPath
          override val logoData       = $logoData
          override val sections       = $sectionTerms
          override val owner          = $owner
          override val repository     = $repository
          override val timestamp      = $timestamp
          override val buildMetaInfo  = $buildInfoT
        }"""
  }

  def makePackage(
      packageName: String,
      trees: List[Tree]
  ) = q"""
        package ${makeRefTree(packageName)} {
          import org.scalaexercises.runtime.model.{ Exercise, Library, Section, Contribution, BuildInfo }
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
      case Nil          ⇒
        // This situation should never occur, and definitely not during
        // recursion of this method. Assume we got here because path.split
        // below resulted in an empty list.
        Ident(TermName(path))
    }
    go(path.split('.').toList.reverse)
  }

}
