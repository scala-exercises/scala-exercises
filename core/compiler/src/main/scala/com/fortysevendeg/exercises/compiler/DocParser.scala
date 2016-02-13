package com.fortysevendeg.exercises
package compiler

import scala.reflect.internal.Chars.isWhitespace

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
    description: String
  )

  def parseSectionDocComment(comment: SourceTextExtraction#ExtractedComment): Xor[String, ParsedSectionComment] =
    for {
      name ← renderSummary(comment.comment)
      description ← renderComment(comment.comment)
    } yield ParsedSectionComment(
      name = name,
      description = description
    )

  case class ParsedExerciseComment(
    name:        Option[String],
    description: Option[String],
    explanation: Option[String]
  )

  def parseExerciseDocComment(comment: SourceTextExtraction#ExtractedComment): Xor[String, ParsedExerciseComment] =
    Xor right ParsedExerciseComment(
      name = renderSummary(comment.comment).toOption,
      description = renderComment(comment.comment).toOption,
      explanation = None
    )

  // ~ BEGIN
  // The following methods are strongly based off of code in
  // scala.tools.nsc.util.DocStrings. I chose to copy the methods here
  // so adjustments could be made for our purposes. A bastardization, of sorts.

  // Adjusted from DocStrings.skipToEol
  private def skipToEol(str: String, start: Int): Int =
    if (start + 2 < str.length && (str charAt start) == '/' && (str charAt (start + 1)) == '*' && (str charAt (start + 2)) == '*') start + 3
    else if (start + 1 < str.length && (str charAt start) == '*' && (str charAt (start + 1)) == '/') start
    else if (start < str.length && (str charAt start) != '\n') skipToEol(str, start + 1)
    else start

  // Our own creation
  private def trimTrailingWhitespace(str: String, start: Int, end: Int): Int = {
    if (end > start && isWhitespace(str charAt (end - 1))) trimTrailingWhitespace(str, start, end - 1)
    else end
  }

  // Verbatim copy of DocStrings.skipLineLead
  private def skipLineLead(str: String, start: Int): Int =
    if (start == str.length) start
    else {
      val idx = skipWhitespace(str, start + 1)
      if (idx < str.length && (str charAt idx) == '*') skipWhitespace(str, idx + 1)
      else if (idx + 2 < str.length && (str charAt idx) == '/' && (str charAt (idx + 1)) == '*' && (str charAt (idx + 2)) == '*')
        skipWhitespace(str, idx + 3)
      else idx
    }

  // Verbatim copy of DocStrings.skipWhitespace
  private def skipWhitespace(str: String, start: Int): Int =
    if (start < str.length && isWhitespace(str charAt start)) skipWhitespace(str, start + 1)
    else start

  // ~ END

  private[compiler] def cleanLines(lines: List[String]): List[String] = {
    lines.map { line ⇒
      val ll = skipLineLead(line, -1)
      val le0 = skipToEol(line, ll)
      val le1 = trimTrailingWhitespace(line, ll, le0)
      line.substring(ll, le1)
    }
      .dropWhile(_.isEmpty)
      .reverse
      .dropWhile(_.isEmpty)
      .dropWhile(_ == "/")
      .dropWhile(_.isEmpty)
      .reverse
  }

  private[compiler] def cleanLines(blob: String): List[String] =
    cleanLines(blob.lines.toList)

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

  def blockToHtml(block: Block)(implicit skipSummary: Boolean): NodeSeq = block match {
    case Title(in, 1)                                        ⇒ <h3>{ inlineToHtml(in) }</h3>
    case Title(in, 2)                                        ⇒ <h4>{ inlineToHtml(in) }</h4>
    case Title(in, 3)                                        ⇒ <h5>{ inlineToHtml(in) }</h5>
    case Title(in, _)                                        ⇒ <h6>{ inlineToHtml(in) }</h6>
    case Paragraph(Chain(Summary(in) :: Nil)) if skipSummary ⇒ Nil
    case Paragraph(in)                                       ⇒ <p>{ inlineToHtml(in) }</p>
    case Code(data) ⇒
      <pre class={ "scala" }>{ data }</pre>
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
