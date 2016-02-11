package com.fortysevendeg.exercises
package compiler

import scala.reflect.internal.Chars.isWhitespace

import cats.data.Xor

/** Handles parsing doc comment strings into friendly data structures
  * containing the relevant information need by the exercise compiler.
  */
object DocParser {

  case class ParsedLibraryComment(
    name:        String,
    description: String
  )

  def parseLibraryDocComment(comment: String): Xor[String, ParsedLibraryComment] = {
    cleanLines(comment.lines.toList) match {
      case name :: Nil ⇒
        Xor.left("Library comment is missing description after the first line")

      case name :: descriptionLines ⇒
        Xor.right(ParsedLibraryComment(
          name = name,
          description = descriptionLines.mkString(" ")
        ))
      case _ ⇒ Xor.left("Library comment is missing name and description")
    }
  }

  case class ParsedSectionComment(
    name:        String,
    description: String
  )

  def parseSectionDocComment(comment: String): Xor[String, ParsedSectionComment] = {
    cleanLines(comment.lines.toList) match {
      case name :: Nil ⇒
        Xor.left("Section comment is missing description after the first line")

      case name :: descriptionLines ⇒
        Xor.right(ParsedSectionComment(
          name = name,
          description = descriptionLines.mkString(" ")
        ))
      case _ ⇒
        Xor.left("Section comment is missing name and description")
    }
  }

  case class ParsedExerciseComment(
    name:        Option[String],
    description: Option[String],
    explanation: Option[String]
  )

  def parseExerciseDocComment(comment: String): Xor[String, ParsedExerciseComment] = {
    cleanLines(comment.lines.toList) match {
      case name :: Nil ⇒
        Xor.right(ParsedExerciseComment(
          name = Some(name),
          description = None,
          explanation = None
        ))
      case name :: descriptionLines ⇒
        Xor.right(ParsedExerciseComment(
          name = Some(name),
          description = Some(descriptionLines.mkString(" ")),
          explanation = None
        ))
      case _ ⇒
        Xor.right(ParsedExerciseComment(
          name = None,
          description = None,
          explanation = None
        ))
    }
  }

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
