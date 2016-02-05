package com.fortysevendeg.exercises
package compiler

import scala.tools.nsc.util.DocStrings

import cats._
import cats.data.Xor
import cats.std.all._

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

  private def skipToEolEx(str: String, start: Int): Int =
    if (start + 2 < str.length && (str charAt start) == '/' && (str charAt (start + 1)) == '*' && (str charAt (start + 2)) == '*') start + 3
    else if (start + 1 < str.length && (str charAt start) == '*' && (str charAt (start + 1)) == '/') start
    else if (start < str.length && (str charAt start) != '\n') skipToEolEx(str, start + 1)
    else start

  private def trimTrailingWhitespace(str: String, start: Int, end: Int): Int =
    if (end >= start && (str charAt (end - 1)) == ' ') trimTrailingWhitespace(str, start, end - 1)
    else end

  private def cleanLines(lines: List[String]): List[String] = {
    lines.map { line ⇒
      val ll = DocStrings.skipLineLead(line, -1)
      val le0 = skipToEolEx(line, ll)
      val le1 = trimTrailingWhitespace(line, ll, le0)
      line.substring(ll, le1)
    }
      .dropWhile(_.isEmpty)
      .reverse
      .dropWhile(_ == "/")
      .dropWhile(_.isEmpty)
      .reverse
  }

}
