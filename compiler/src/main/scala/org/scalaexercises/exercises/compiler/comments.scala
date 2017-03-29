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

import scala.language.higherKinds

import scala.tools.nsc._
import scala.tools.nsc.doc.base.CommentFactoryBase
import scala.tools.nsc.doc.base.MemberLookupBase
import scala.tools.nsc.doc.base.LinkTo
import scala.tools.nsc.doc.base.LinkToExternal
import scala.tools.nsc.doc.base.comment._

import scala.xml.NodeSeq
import scala.xml.Xhtml

import cats.{Eq, Functor, Id}
import cats.implicits._

import org.scalaexercises.compiler.formatting._

/** Facade for the different layers of comment processing. */
object Comments {
  import CommentZed._
  import CommentParsing.ParseK

  /** Type capturing the types for the name, description,
   * and explanation fields.
   */
  type Mode = {
    type Name[A]
    type Description[A]
    type Explanation[A]
  }

  object Mode {
    type Aux[N0[_], D0[_], E0[_]] = Mode {
      type Name[A]        = N0[A]
      type Description[A] = D0[A]
      type Explanation[A] = E0[A]
    }

    /** Library comments. Required name. Required description.
     * Require no explanation.
     */
    type Library = Aux[Id, Id, Empty]

    /** Section comments. Require name. Optional description.
     * Require no explanation.
     */
    type Section = Aux[Id, Option, Empty]

    /** Exercise comments. Optional name. Optional description.
     * Optional explanation.
     */
    type Exercise = Aux[Option, Option, Option]

    // Isolated parse modes for specific fields.
    // Each one ignores everything except the respective field.
    // Note -- this is mainly used for testing.
    type Name[A[_]]        = Aux[A, Ignore, Ignore]
    type Description[A[_]] = Aux[Ignore, A, Ignore]
    type Explanation[A[_]] = Aux[Ignore, Ignore, A]
  }

  /** Helper function to parse and render a given comment. */
  def parseAndRender[A <: Mode](comment: Comment)(
      implicit evPKN: ParseK[A#Name],
      evPKD: ParseK[A#Description],
      evPKE: ParseK[A#Explanation],
      evFD: Functor[A#Description],
      evFE: Functor[A#Explanation]
  ) =
    CommentParsing.parse[A](comment).map(CommentRendering.render(_))

}

/* Our interface for interacting with Scaladoc's comment factory. */
private[compiler] sealed trait CommentFactory[G <: Global] {
  val global: G
  def parse(comment: global.DocComment): Comment
  // at the moment, this is provided for testing
  def parse(comment: String): Comment
}

private[compiler] object CommentFactory {

  /** Create a comment factory instance for a given global. */
  def apply[G <: Global](g: G): CommentFactory[g.type] = {
    new CommentFactoryBase with MemberLookupBase with CommentFactory[g.type] {
      override val global: g.type = g
      import global._
      override def parse(comment: DocComment) = {
        val nowarnings = settings.nowarn.value
        settings.nowarn.value = true
        try parseAtSymbol(comment.raw, comment.raw, comment.pos)
        finally settings.nowarn.value = nowarnings
      }
      override def parse(comment: String)                                              = parse(DocComment(comment))
      override def internalLink(sym: Symbol, site: Symbol): Option[LinkTo]             = None
      override def chooseLink(links: List[LinkTo]): LinkTo                             = links.headOption.orNull
      override def toString(link: LinkTo): String                                      = "No link"
      override def findExternalLink(sym: Symbol, name: String): Option[LinkToExternal] = None
      override def warnNoLink: Boolean                                                 = false
    }
  }

}

/** Special types used for parsing and rendering. */
private[compiler] object CommentZed {

  /** Empty type for values that should raise an error
   * if they are present.
   */
  sealed trait Empty[+A]
  object Empty extends SingletonFunctor[Empty] with Empty[Nothing]

  /** Ignore type for values that we want to completely ignore during
   * parsing.
   */
  sealed trait Ignore[+A]
  object Ignore extends SingletonFunctor[Ignore] with Ignore[Nothing]

  /** Functor, for a singleton type. */
  sealed trait SingletonFunctor[F[+ _]] { instance: F[Nothing] ⇒
    implicit def singletonEq[A]: Eq[F[A]] = new Eq[F[A]] {
      def eqv(a1: F[A], a2: F[A]): Boolean = a1 == a2 // always true?
    }
    implicit val singletonFunctor = new Functor[F] {
      def map[A, B](fa: F[A])(f: A ⇒ B) = instance
    }
  }
}

private[compiler] object CommentParsing {
  import Comments.Mode
  import CommentZed._

  /** Parse value container typeclass */
  trait ParseK[A[_]] {

    /** Take a potential value and map it into the desired
     * type. If the value coming in is `Either.Left`, then the value
     * was not present during parsing. An incoming value of `Either.Right`
     * indicates that a value was parsed. An output of `Either.Left` indicates
     * that an error should be raised. And an output value of `Either.Right`
     * indicates that the value was parsed and mapped into the appropriate
     * type.
     */
    def fromEither[T](value: Either[String, T]): Either[String, A[T]]
  }

  object ParseK {
    def apply[A[_]](implicit instance: ParseK[A]): ParseK[A] = instance

    /** A required value, which is always passed directly through.
     * A value that wasn't present during parsing will raise an error.
     */
    implicit val idParseK = new ParseK[Id] {
      override def fromEither[T](value: Either[String, T]) = value
    }

    /** Parse an optional value. The result is always the right side `Either`
     * projection because the value is optional and shouldn't fail.
     */
    implicit val optionParseK = new ParseK[Option] {
      override def fromEither[T](value: Either[String, T]) =
        Either.right(value.toOption)
    }

    /** Parse a value that shouldn't exist. The input `Either` is swapped
     * so that a parsed input value yields an error and a nonexistant
     * input value yields a success.
     */
    implicit val emptyParseK = new ParseK[Empty] {
      override def fromEither[T](value: Either[String, T]) =
        value.swap.bimap(
          _ ⇒ "Unexpected value",
          _ ⇒ Empty
        )
    }

    /** Parse a value that we're indifferent about. The result is
     * always success with a placeholder value.
     */
    implicit val ignoreParseK = new ParseK[Ignore] {
      override def fromEither[T](either: Either[String, T]) =
        Either.right(Ignore)
    }
  }

  /** A parsed comment with the values stored in the appropriate types
   */
  case class ParsedComment[N[_], D[_], E[_]](
      name: N[String],
      description: D[Body],
      explanation: E[Body]
  )

  object ParsedComment {
    type Aux[A <: Mode] = ParsedComment[A#Name, A#Description, A#Explanation]
  }

  def parse[A <: Mode](comment: Comment)(
      implicit evN: ParseK[A#Name],
      evD: ParseK[A#Description],
      evE: ParseK[A#Explanation]
  ): String Either ParsedComment[A#Name, A#Description, A#Explanation] = parse0(comment)

  private[this] def parse0[N[_]: ParseK, D[_]: ParseK, E[_]: ParseK](
      comment: Comment): String Either ParsedComment[N, D, E] = {
    val params = comment.valueParams

    lazy val nameEither = {
      val name1 = params.get("name").collect {
        case Body(List(Paragraph(Chain(List(Summary(Text(value))))))) ⇒ value.trim
      }
      val name2 = name1.filter(_.lines.length == 1)
      Either.fromOption(
        name2,
        "Expected single name value defined as '@param name <value>'"
      )
    }

    lazy val descriptionEither = comment.body match {
      case Body(Nil) ⇒ Either.left("Unable to parse comment body")
      case body      ⇒ Either.right(body)
    }

    lazy val explanationEither = Either.fromOption(
      params.get("explanation"),
      "Expected explanation defined as '@param explanation <...extended value...>'"
    )

    for {
      name        ← ParseK[N].fromEither(nameEither)
      description ← ParseK[D].fromEither(descriptionEither)
      explanation ← ParseK[E].fromEither(explanationEither)
    } yield
      ParsedComment(
        name = name,
        description = description,
        explanation = explanation
      )

  }

}

private[compiler] object CommentRendering {
  import Comments.Mode
  import CommentParsing.ParsedComment

  /** A rendered comment. This leverages the same types
   * used during parsing.
   */
  case class RenderedComment[N[_], D[_], E[_]](
      name: N[String],
      description: D[String],
      explanation: E[String]
  )

  object RenderedComment {
    type Aux[A <: Mode] = RenderedComment[A#Name, A#Description, A#Explanation]
  }

  def render[N[_], D[_]: Functor, E[_]: Functor](
      parsedComment: ParsedComment[N, D, E]): RenderedComment[N, D, E] =
    RenderedComment(
      name = parsedComment.name,
      description = Functor[D].map(parsedComment.description)(renderBody),
      explanation = Functor[E].map(parsedComment.explanation)(renderBody)
    )

  def renderBody(body: Body): String =
    Xhtml.toXhtml(body.blocks flatMap (renderBlock(_)))

  private[this] def renderBlock(block: Block): NodeSeq = block match {
    case Title(in, 1)  ⇒ <h3>{ renderInline(in) }</h3>
    case Title(in, 2)  ⇒ <h4>{ renderInline(in) }</h4>
    case Title(in, 3)  ⇒ <h5>{ renderInline(in) }</h5>
    case Title(in, _)  ⇒ <h6>{ renderInline(in) }</h6>
    case Paragraph(in) ⇒ <p>{ renderInline(in) }</p>
    case Code(data) ⇒
      <pre class={ "scala" }><code class={ "scala" }>{ formatCode(data) }</code></pre>
    case UnorderedList(items) ⇒
      <ul>{ renderListItems(items) }</ul>
    case OrderedList(items, listStyle) ⇒
      <ol class={ listStyle }>{ renderListItems(items) }</ol>
    case DefinitionList(items) ⇒
      <dl>{ items map { case (t, d) ⇒ <dt>{ renderInline(t) }</dt><dd>{ renderBlock(d) }</dd> } }</dl>
    case HorizontalRule() ⇒
      <hr/>
  }

  private[this] def renderListItems(items: Seq[Block]) =
    items.foldLeft(xml.NodeSeq.Empty) { (xmlList, item) ⇒
      item match {
        case OrderedList(_, _) |
            UnorderedList(_) ⇒ // html requires sub ULs to be put into the last LI
          xmlList.init ++ <li>{ xmlList.last.child ++ renderBlock(item) }</li>
        case Paragraph(inline) ⇒
          xmlList :+ <li>{ renderInline(inline) }</li> // LIs are blocks, no need to use Ps
        case block ⇒
          xmlList :+ <li>{ renderBlock(block) }</li>
      }
    }

  private[this] def renderInline(inl: Inline): NodeSeq = inl match {
    case Chain(items)             ⇒ items flatMap (renderInline(_))
    case Italic(in)               ⇒ <i>{ renderInline(in) }</i>
    case Bold(in)                 ⇒ <b>{ renderInline(in) }</b>
    case Underline(in)            ⇒ <u>{ renderInline(in) }</u>
    case Superscript(in)          ⇒ <sup>{ renderInline(in) }</sup>
    case Subscript(in)            ⇒ <sub>{ renderInline(in) }</sub>
    case Link(raw, title)         ⇒ <a href={ raw } target="_blank">{ renderInline(title) }</a>
    case Monospace(in)            ⇒ <code>{ renderInline(in) }</code>
    case Text(text)               ⇒ scala.xml.Text(text)
    case Summary(in)              ⇒ renderInline(in)
    case HtmlTag(tag)             ⇒ scala.xml.Unparsed(tag)
    case EntityLink(target, link) ⇒ renderLink(target, link, hasLinks = true)
  }

  private[this] def renderLink(text: Inline, link: LinkTo, hasLinks: Boolean) = renderInline(text)

}
