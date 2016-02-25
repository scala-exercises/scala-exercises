package stdlib

import org.scalatest._

/** literal_strings
  *
  */
object LiteralStrings extends FlatSpec with Matchers with exercise.Section {


  /** Character Literals are quoted with single quotes:
    */
  def characterLiteralsLiteralStrings(res0: String, res1: String) {
    val a = 'a'
    val b = 'B'

    a.toString should be(res0)
    b.toString should be(res1)
  }

  /** Character Literals can use hexadecimal Unicode
    */
  def characterLiteralsUnicodeLiteralStrings(res0: String) {
    val c = '\u0061' //unicode for a

    c.toString should be(res0)
  }

  /** Character Literals can use octal as well
    */
  def characterLiteralsOctalLiteralStrings(res0: String) {
    val d = '\141' //octal for a
    d.toString should be(res0)
  }

  /** Character Literals can use escape sequences
    */
  def escapeSequenceLiteralStrings(res0: String, res1: String) {
    val e = '\"'
    val f = '\\'

    e.toString should be(res0)
    f.toString should be(res1)
  }

  /** One-Line String Literals are surrounded by quotation marks.
    */
  def oneLineLiteralStrings(res0: String) {
    val a = "To be or not to be"
    a should be(res0)
  }

  //TODO Support multiline comments
  //  /** multilineLiteralStrings
  //    *
  //    * Multiline String literals are surrounded by three quotation marks.
  //    */
  //  def multilineLiteralStrings(res0: Int) {
  //    val a =
  //      """An apple a day
  //keeps the doctor away"""
  //    a.split('\n').size should be(res0) //a.split('\n').size determines the number of lines
  //  }
  //
  //  /** stripMarginLiteralStrings
  //    *
  //    * Use stripMargin to prettify multi-line strings:
  //    */
  //  def stripMarginLiteralStrings(res0: String) {
  //    /*
  //* Multiline String literals can use | to specify the starting position
  //* of subsequent lines, then use stripMargin to remove the surplus indentation.
  //*/
  //
  //    val a =
  //      """An apple a day
  //        |keeps the doctor away"""
  //    a.stripMargin.split('\n')(1).charAt(0) should be(res0)
  //
  //    /*
  //    * a.stripMargin.split('\n')(1).charAt(0)
  //    * gets the first character of the second line
  //    */
  //  }

}
