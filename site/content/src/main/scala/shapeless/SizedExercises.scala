package shapelessex

import org.scalatest._
import shapeless._

/** == Collections with statically known sizes ==
  *
  * shapeless provides collection types with statically known sizes. These can prevent runtime errors such as those that
  * would result from attempting to take the head of an empty list, and can also verify more complex relationships.
  *
  * {{{
  * def row(cols : Seq[String]) =
  * cols.mkString("\\"", "\\", \\"", "\\"")
  *
  * def csv[N <: Nat]
  * (hdrs : Sized[Seq[String], N],
  * rows : List[Sized[Seq[String], N]]) = row(hdrs) :: rows.map(row(_))
  *
  * val hdrs = Sized("Title", "Author")
  *
  * val rows = List(
  * Sized("Types and Programming Languages", "Benjamin Pierce"),
  * Sized("The Implementation of Functional Programming Languages", "Simon Peyton-Jones")
  * )
  * }}}
  *
  * @param name sized
  */
object SizedExercises extends FlatSpec with Matchers with exercise.Section {

  object Helper {
    def row(cols: Seq[String]) =
      cols.mkString("\"", "\", \"", "\"")

    def csv[N <: Nat](hdrs: Sized[Seq[String], N], rows: List[Sized[Seq[String], N]]) =
      row(hdrs) :: rows.map(row(_))

    val hdrs = Sized("Title", "Author")

    val rows = List(
      Sized("Types and Programming Languages", "Benjamin Pierce"),
      Sized("The Implementation of Functional Programming Languages", "Simon Peyton-Jones")
    )
  }

  import Helper._

  /** In the example below we define a method `csv` whose signature guarantees at compile time that there are exactly as many
    * column headers provided as colums
    * TODO : what would be a good exercise for stuff that can only be proven at compile time???
    */
  def sizedEx(res0: Int) = {

    // hdrs and rows statically known to have the same number of columns
    val formatted = csv(hdrs, rows) // Compiles

    // extendedHdrs has the wrong number of columns for rows
    val extendedHdrs = Sized("Title", "Author", "ISBN")
    //val badFormatted = csv(extendedHdrs, rows)             // Does not compile
    res0 should be(1)
  }

}
