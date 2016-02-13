package shapelessex

import org.scalatest._
import shapeless._
import ops.hlist._

object size extends Poly1 {
  implicit def caseInt = at[Int](x => 1)
  implicit def caseString = at[String](_.length)
  implicit def caseTuple[T, U]
    (implicit st : Case.Aux[T, Int], su : Case.Aux[U, Int]) =
      at[(T, U)](t => size(t._1)+size(t._2))
}

object addSize extends Poly2 {
  implicit  def default[T](implicit st: shapelessex.size.Case.Aux[T, Int]) =
    at[Int, T]{ (acc, t) => acc+size(t) }
    }


/** Heterogenous lists
  *
  * shapeless provides a comprehensive Scala `HList` which has many features not shared by other HList implementations.


  */
object HListExercises extends FlatSpec with Matchers with exercise.Section {

  /** Map
    *
    *  It has a `map` operation, applying a polymorphic function value across its elements. This means that it subsumes both
    * typical `HList`'s and also `KList`'s (`HList`'s whose elements share a common outer type constructor).
    */
  def exerciseMap(res0: Option[Int], res1 : Option[String]) = {
    import poly._

    object choose extends (Set ~> Option) {
      def apply[T](s : Set[T]) = s.headOption
    }

    val sets = Set(1) :: Set("foo") :: HNil

    val opts = sets map choose

    opts should be (res0 :: res1 :: HNil)
  }

  /** Flat Map
    *
    * It also has a flatMap Operation
    */
  def exerciseFlatMap(res0 : Int, res1 : String, res2 : Boolean) = {
    import poly.identity

    val l = (23 :: "foo" :: HNil) :: HNil :: (true :: HNil) :: HNil

    l flatMap identity should be (res0 :: res1 :: res2 :: HNil)

  }

  /** Polymorphic fold
    *
    * It has a set of fully polymorphic fold operations which take a polymorphic binary function value. The fold is sensitive
    * to the static types of all of the elements of the `HList`. Given the earlier definition of size,
    * {{{
    * object addSize extends Poly2 {
    * implicit  def default[T](implicit st: shapelessex.size.Case.Aux[T, Int]) =
    * at[Int, T]{ (acc, t) => acc+size(t) }
    * }
    * }}}
    */
  def exerciseFold(res0 : Int) = {


    val l = 23 :: "foo" :: (13, "wibble") :: HNil

    l.foldLeft(0)(addSize) should be (res0)

  }


  /** Zipper
    *
    * It also has a zipper for traversal and persistent update
    */
  def exerciseZipper(res0 : Int, res1 : (String, Int), res2 : Double, res3 : Int, res4 : String, res5 : String, res6 : Double) = {
    import syntax.zipper._

    val l = 1 :: "foo" :: 3.0 :: HNil
    l.toZipper.right.put(("wibble", 45)).reify should be (res0 :: res1 :: res2 :: HNil)

    l.toZipper.right.delete.reify should be (res3 :: res4 :: res5 :: res6 :: HNil)

  }
}
