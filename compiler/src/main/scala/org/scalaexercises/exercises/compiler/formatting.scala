package org.scalaexercises.compiler

import scalariform.formatter.{ ScalaFormatter }

import cats.data.Xor

object formatting {
  private[this] def wrap(code: String): String = s"""// format: OFF
      |object Wrapper {
      |  // format: ON
      |  $code
      |  // format: OFF
      |}""".stripMargin

  private[this] def unwrap(code: String): String =
    code.split("\n")
      .drop(3)
      .dropRight(2)
      .map(_.drop(2)).mkString("\n")

  def formatCode(code: String): String = {
    Xor.catchNonFatal(ScalaFormatter.format(wrap(code))) match {
      case Xor.Right(result) ⇒ unwrap(result)
      case _                 ⇒ code
    }
  }
}
