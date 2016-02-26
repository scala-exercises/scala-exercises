/*
 * scala-exercises-exercise-compiler
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises
package compiler

import scala.language.higherKinds

import scala.tools.nsc._
import scala.tools.nsc.doc.base.CommentFactoryBase
import scala.tools.nsc.doc.base.MemberLookupBase
import scala.tools.nsc.doc.base.LinkTo
import scala.tools.nsc.doc.base.LinkToExternal
import scala.tools.nsc.doc.base.comment._

import scala.xml.NodeSeq
import scala.xml.Xhtml

import scalariform.formatter.{ ScalaFormatter }

import cats.{ Id, Monad }
import cats.data.Xor
import cats.syntax.option._

private[compiler] sealed trait CommentFactory[G <: Global] {
  val global: G
  def parse(comment: global.DocComment): Comment
  // at the moment, this is provided for testing
  def parse(comment: String): Comment
}

object CommentFactory {

  private[compiler] def apply[G <: Global](g: G): CommentFactory[g.type] = {
    new CommentFactoryBase with MemberLookupBase with CommentFactory[g.type] {
      override val global: g.type = g
      import global._
      override def parse(comment: DocComment) = {
        val nowarnings = settings.nowarn.value
        settings.nowarn.value = true
        try parseAtSymbol(comment.raw, comment.raw, comment.pos)
        finally settings.nowarn.value = nowarnings
      }
      override def parse(comment: String) = parse(DocComment(comment))
      override def internalLink(sym: Symbol, site: Symbol): Option[LinkTo] = None
      override def chooseLink(links: List[LinkTo]): LinkTo = links.headOption.orNull
      override def toString(link: LinkTo): String = "No link"
      override def findExternalLink(sym: Symbol, name: String): Option[LinkToExternal] = None
      override def warnNoLink: Boolean = false
    }
  }

}

object CommentParsing {

  type Empty[A] = { type EMPTY }
  val Empty: Empty[Nothing] = new { type EMPTY = Unit }

  type Ignore[A] = { type IGNORE }
  val Ignore: Ignore[Nothing] = new { type IGNORE = Unit }

  trait ParseK[A[_]] {
    def fromXor[T](xor: Xor[String, T]): Xor[String, A[T]]
  }

  object ParseK {
    def apply[A[_]](implicit instance: ParseK[A]): ParseK[A] = instance

    implicit val idParseK = new ParseK[Id] {
      override def fromXor[T](xor: Xor[String, T]) = xor
    }

    implicit val optionParseK = new ParseK[Option] {
      override def fromXor[T](xor: Xor[String, T]) =
        Xor.right(xor.toOption)
    }

    implicit val emptyParseK = new ParseK[Empty] {
      override def fromXor[T](xor: Xor[String, T]) =
        xor.swap.bimap(
          _ ⇒ "Unexpected value",
          _ ⇒ Empty
        )
    }

    implicit val ignoreParseK = new ParseK[Ignore] {
      override def fromXor[T](xor: Xor[String, T]) =
        Xor.right(Ignore)
    }
  }

  type ParseMode = {
    type N[A]
    type D[B]
    type E[B]
  }

  object ParseMode {
    type Aux[N0[_], D0[_], E0[_]] = ParseMode {
      type N[A] = N0[A]
      type D[A] = D0[A]
      type E[A] = E0[A]
    }
    type Library = ParseMode.Aux[Id, Id, Option]
    type Section = ParseMode.Aux[Option, Id, Option]
    type Exercise = ParseMode.Aux[Option, Option, Option]

    type Name[A[_]] = ParseMode.Aux[A, Ignore, Ignore]
    type Description[A[_]] = ParseMode.Aux[Ignore, A, Ignore]
    type Explanation[A[_]] = ParseMode.Aux[Ignore, Ignore, A]
  }

  case class ParsedComment[N[_], D[_], E[_]](
    name:        N[String],
    description: D[Body],
    explanation: E[Body]
  )

  def parse[A <: ParseMode](comment: Comment)(
    implicit
    evN: ParseK[A#N],
    evD: ParseK[A#D],
    evE: ParseK[A#E]
  ): String Xor ParsedComment[A#N, A#D, A#E] = parseRaw(comment)

  def parseRaw[N[_]: ParseK, D[_]: ParseK, E[_]: ParseK](comment: Comment): String Xor ParsedComment[N, D, E] = {
    val params = comment.valueParams

    lazy val nameXor = {
      val name1 = params.get("name").collect {
        case Body(List(Paragraph(Chain(List(Summary(Text(value))))))) ⇒ value.trim
      }
      val name2 = name1.filter(_.lines.length == 1)

      Xor.fromOption(name2, "Expected single name value defined as '@param name <value>'")
    }

    lazy val descriptionXor = comment.body match {
      case Body(Nil) ⇒ Xor.left("Unable to parse comment body")
      case body      ⇒ Xor.right(body)
    }

    lazy val explanationXor = Xor.fromOption(
      params.get("explanation"), "Expected explanation defined as '@param explanation <...extended value...>'"
    )

    for {
      name ← ParseK[N].fromXor(nameXor)
      description ← ParseK[D].fromXor(descriptionXor)
      explanation ← ParseK[E].fromXor(explanationXor)
    } yield ParsedComment(
      name = name,
      description = description,
      explanation = explanation
    )

  }

}

object CommentRendering {
  import CommentParsing.ParsedComment

  case class RenderedComment[N[_], D[_], E[_]](
    name:        N[String],
    description: D[String],
    explanation: E[String]
  )

  def render[N[_], D[_]: Monad, E[_]: Monad](parsedComment: ParsedComment[N, D, E]): RenderedComment[N, D, E] =
    RenderedComment(
      name = parsedComment.name,
      description = Monad[D].map(parsedComment.description)(render),
      explanation = Monad[E].map(parsedComment.explanation)(render)
    )

  def render(body: Body): String =
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

  private[this] def formatCode(code: String): String = {
    def wrap(code: String): String = s"""object Wrapper { $code }"""
    def unwrap(code: String): String =
      code.split("\n").drop(1).dropRight(1).map(_.drop(2)).mkString("\n")

    Xor.catchNonFatal(ScalaFormatter.format(wrap(code))) match {
      case Xor.Right(result) ⇒ unwrap(result)
      case _                 ⇒ code
    }
  }

  private[this] def renderListItems(items: Seq[Block]) =
    items.foldLeft(xml.NodeSeq.Empty) { (xmlList, item) ⇒
      item match {
        case OrderedList(_, _) | UnorderedList(_) ⇒ // html requires sub ULs to be put into the last LI
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
