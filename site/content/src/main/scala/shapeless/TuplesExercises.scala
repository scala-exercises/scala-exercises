package shapelessex

import org.scalatest._
import shapeless._

/** == HList-style operations on standard Scala tuples ==
  *
  * shapeless allows standard Scala tuples to be manipulated in exactly the same ways as `HList`s
  * @param name tuples
  */
object TuplesExercises extends FlatSpec with Matchers with exercise.Section {

  import syntax.std.tuple._

  /** {{{
    * import syntax.std.tuple._
    * }}}
    * head
    */
  def head(res0: Int) = {
    (23, "foo", true).head should be(res0)
  }

  /** tail
    */
  def tail(res0: (String, Boolean)) = {
    (23, "foo", true).tail should be(res0)
  }

  /** drop
    */
  def drop(res0: Tuple1[Boolean]) = {
    (23, "foo", true).drop(2) should be(res0)
  }

  /** take
    */
  def take(res0: (Int, String)) = {
    (23, "foo", true).take(2) should be(res0)
  }

  /** split
    */
  def split(res0: ((Int), (String, Boolean))) = {
    (23, "foo", true).split(1) should be(res0)
  }

  /** prepend
    */
  def prepend(res0: (Int, String, Boolean)) = {
    val l = 23 +: ("foo", true)
    l should be(res0)
  }

  /** append
    */
  def append(res0: (Int, String, Boolean)) = {
    val l = (23, "foo") :+ true
    l should be(res0)
  }

  /** concatenate
    */
  def concatenate(res0: (Int, String, Boolean, Double)) = {
    val l = (23, "foo") ++ (true, 2.0)
    l should be(res0)
  }

  import poly._

  private[this] object option extends (Id ~> Option) {
    def apply[T](t: T) = Option(t)
  }

  /** map
    * {{{
    * import poly._
    *
    * object option extends (Id ~> Option) {
    * def apply[T](t: T) = Option(t)
    * }
    * }}}
    */
  def map(res0: (Option[Int], Option[String], Option[Boolean])) = {
    val l = (23, "foo", true) map option
    l should be(res0)
  }

  /** flatMap
    */
  def flatMap(res0: (Int, String, Boolean, Double)) = {
    val l = ((23, "foo"), (), (true, 2.0)) flatMap identity
    l should be(res0)
  }

  object sizeOf extends Poly1 {
    implicit def caseInt = at[Int](x ⇒ 1)
    implicit def caseString = at[String](_.length)
    implicit def caseTuple[T, U](implicit st: Case.Aux[T, Int], su: Case.Aux[U, Int]) =
      at[(T, U)](t ⇒ sizeOf(t._1) + sizeOf(t._2))
  }

  object addSize extends Poly2 {
    implicit def default[T](implicit st: sizeOf.Case.Aux[T, Int]) =
      at[Int, T] { (acc, t) ⇒ acc + sizeOf(t) }
  }

  /** fold
    * {{{
    * object size extends Poly1 {
    * implicit def caseInt = at[Int](x => 1)
    * implicit def caseString = at[String](_.length)
    * implicit def caseTuple[T, U]
    * (implicit st : Case.Aux[T, Int], su : Case.Aux[U, Int]) =
    * at[(T, U)](t => size(t._1)+size(t._2))
    * }
    *
    * object addSize extends Poly2 {
    * implicit def default[T](implicit st: size.Case.Aux[T, Int]) =
    * at[Int, T]{ (acc, t) => acc+size(t) }
    * }
    * }}}
    */
  def fold(res0: Int) = {
    (23, "foo", (13, "wibble")).foldLeft(0)(addSize) should be(res0)
  }

  /** conversion to `HList`
    */
  def toHList(res0: Int :: String :: Boolean :: HNil) = {
    (23, "foo", true).productElements should be(res0)
  }

  /** conversion to `List`
    */
  def toList(res0: List[Any]) = {
    (23, "foo", true).toList should be(res0)
  }

  /** zipper
    */
  def zipper(res0: (Int, (String, Boolean), Double)) = {
    import syntax.zipper._
    val l = (23, ("foo", true), 2.0).toZipper.right.down.put("bar").root.reify
    l should be(res0)
  }

}
