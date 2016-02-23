/*
 * scala-exercises-exercise-compiler
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises
package compiler

import scala.reflect.internal.Chars.isWhitespace
import scalariform.formatter.{ ScalaFormatter }

import cats.data.Xor
import cats.syntax.option._

/** Handles parsing doc comment strings into friendly data structures
  * containing the relevant information need by the exercise compiler.
  */
object DocParser extends DocRendering {

  case class ParsedLibraryComment(
    name:        String,
    description: String
  )

  def parseLibraryDocComment(comment: SourceTextExtraction#ExtractedComment): Xor[String, ParsedLibraryComment] =
    for {
      name ← renderSummary(comment.comment)
      description ← renderComment(comment.comment)
    } yield ParsedLibraryComment(
      name = name,
      description = description
    )

  case class ParsedSectionComment(
    name:        String,
    description: Option[String]
  )

  def parseSectionDocComment(comment: SourceTextExtraction#ExtractedComment): Xor[String, ParsedSectionComment] =
    for {
      name ← renderSummary(comment.comment)
    } yield ParsedSectionComment(
      name = name,
      description = renderComment(comment.comment).toOption
    )

  case class ParsedExerciseComment(
    name:        String,
    description: Option[String],
    explanation: Option[String]
  )

  def parseExerciseDocComment(comment: SourceTextExtraction#ExtractedComment): Xor[String, ParsedExerciseComment] =
    for {
      name ← renderSummary(comment.comment)
    } yield ParsedExerciseComment(
      name = name,
      description = renderComment(comment.comment).toOption,
      explanation = None
    )

}

sealed trait DocRendering {

  // TODO: this is a quick-n-dirty implementation and needs to be revisited
  // - It's a huge rip from scala.tools.nsc.doc.html.HtmlPage
  // - I'd like to avoid using scala.xml
  // - I cheated to avoid rendering the Summary, and it's not very elegant
  //
  // ...but... it works

  import scala.tools.nsc.doc.base.LinkTo
  import scala.tools.nsc.doc.base.comment._
  import scala.xml.NodeSeq
  import scala.xml.Xhtml

  def renderSummary(comment: Comment): Xor[String, String] =
    comment.body.summary.map(inlineToHtml(_)(false)).map(Xhtml.toXhtml)
      .toRightXor("no summary found in comment")

  def renderComment(comment: Comment): Xor[String, String] = {
    val nodes = bodyToHtml(comment.body)(true)
    if (nodes.isEmpty) Xor.left("missing body for comment")
    else Xor.right(Xhtml.toXhtml(nodes))
  }

  def bodyToHtml(body: Body)(implicit skipSummary: Boolean): NodeSeq =
    body.blocks flatMap (blockToHtml(_))

  def wrapCode(code: String): String =
    s"""object Wrapper {
     $code
    }"""

  def unwrapCode(code: String): String = {
    code.split("\n").drop(1).dropRight(1).map(_.drop(2)).mkString("\n")
  }

  def formatCode(code: String): String = {
    Xor.catchNonFatal(
      ScalaFormatter.format(wrapCode(code))
    ) match {
        case Xor.Right(result) ⇒ unwrapCode(result)
        case _                 ⇒ code
      }
  }

  def blockToHtml(block: Block)(implicit skipSummary: Boolean): NodeSeq = block match {
    case Title(in, 1)                                        ⇒ <h3>{ inlineToHtml(in) }</h3>
    case Title(in, 2)                                        ⇒ <h4>{ inlineToHtml(in) }</h4>
    case Title(in, 3)                                        ⇒ <h5>{ inlineToHtml(in) }</h5>
    case Title(in, _)                                        ⇒ <h6>{ inlineToHtml(in) }</h6>
    case Paragraph(Chain(Summary(in) :: Nil)) if skipSummary ⇒ Nil
    case Paragraph(in)                                       ⇒ <p>{ inlineToHtml(in) }</p>
    case Code(data) ⇒
      <pre class={ "scala" }>{ formatCode(data) }</pre>
    case UnorderedList(items) ⇒
      <ul>{ listItemsToHtml(items) }</ul>
    case OrderedList(items, listStyle) ⇒
      <ol class={ listStyle }>{ listItemsToHtml(items) }</ol>
    case DefinitionList(items) ⇒
      <dl>{ items map { case (t, d) ⇒ <dt>{ inlineToHtml(t) }</dt><dd>{ blockToHtml(d) }</dd> } }</dl>
    case HorizontalRule() ⇒
      <hr/>
  }

  def listItemsToHtml(items: Seq[Block])(implicit skipSummary: Boolean) =
    items.foldLeft(xml.NodeSeq.Empty) { (xmlList, item) ⇒
      item match {
        case OrderedList(_, _) | UnorderedList(_) ⇒ // html requires sub ULs to be put into the last LI
          xmlList.init ++ <li>{ xmlList.last.child ++ blockToHtml(item) }</li>
        case Paragraph(inline) ⇒
          xmlList :+ <li>{ inlineToHtml(inline) }</li> // LIs are blocks, no need to use Ps
        case block ⇒
          xmlList :+ <li>{ blockToHtml(block) }</li>
      }
    }

  def inlineToHtml(inl: Inline)(implicit skipSummary: Boolean): NodeSeq = inl match {
    case Chain(items)             ⇒ items flatMap (inlineToHtml(_))
    case Italic(in)               ⇒ <i>{ inlineToHtml(in) }</i>
    case Bold(in)                 ⇒ <b>{ inlineToHtml(in) }</b>
    case Underline(in)            ⇒ <u>{ inlineToHtml(in) }</u>
    case Superscript(in)          ⇒ <sup>{ inlineToHtml(in) }</sup>
    case Subscript(in)            ⇒ <sub>{ inlineToHtml(in) }</sub>
    case Link(raw, title)         ⇒ <a href={ raw } target="_blank">{ inlineToHtml(title) }</a>
    case Monospace(in)            ⇒ <code>{ inlineToHtml(in) }</code>
    case Text(text)               ⇒ scala.xml.Text(text)
    case Summary(in)              ⇒ if (skipSummary) Nil else inlineToHtml(in)
    case HtmlTag(tag)             ⇒ scala.xml.Unparsed(tag)
    case EntityLink(target, link) ⇒ linkToHtml(target, link, hasLinks = true)
  }

  def linkToHtml(text: Inline, link: LinkTo, hasLinks: Boolean)(implicit skipSummary: Boolean) = inlineToHtml(text)

}
