package shapelessex

import org.scalatest._
import shapeless._
import ops.hlist._

object size extends Poly1 {
  implicit def caseInt = at[Int](x ⇒ 1)
  implicit def caseString = at[String](_.length)
  implicit def caseTuple[T, U](implicit st: Case.Aux[T, Int], su: Case.Aux[U, Int]) =
    at[(T, U)](t ⇒ size(t._1) + size(t._2))
}

object addSize extends Poly2 {
  implicit def default[T](implicit st: shapelessex.size.Case.Aux[T, Int]) =
    at[Int, T] { (acc, t) ⇒ acc + size(t) }
}

object CovariantHelper {

  trait Fruit
  case class Apple() extends Fruit
  case class Pear() extends Fruit

  type FFFF = Fruit :: Fruit :: Fruit :: Fruit :: HNil
  type APAP = Apple :: Pear :: Apple :: Pear :: HNil

  val a: Apple = Apple()
  val p: Pear = Pear()

  val apap: APAP = a :: p :: a :: p :: HNil
}

/** == Heterogenous lists ==
  *
  * shapeless provides a comprehensive Scala `HList` which has many features not shared by other HList implementations.
  *
  * It has a `map` operation, applying a polymorphic function value across its elements. This means that it subsumes both
  * typical `HList`'s and also `KList`'s (`HList`'s whose elements share a common outer type constructor).
  *
  * @param name heterogenous_lists
  */
object HListExercises extends FlatSpec with Matchers with exercise.Section {

  /** It has a `map` operation, applying a polymorphic function value across its elements. This means that it subsumes both
    * typical `HList`'s and also `KList`'s (`HList`'s whose elements share a common outer type constructor).
    */
  def exerciseMap(res0: Option[Int], res1: Option[String]) = {
    import poly._

    object choose extends (Set ~> Option) {
      def apply[T](s: Set[T]) = s.headOption
    }

    val sets = Set(1) :: Set("foo") :: HNil

    val opts = sets map choose

    opts should be(res0 :: res1 :: HNil)
  }

  /** It also has a flatMap Operation
    */
  def exerciseFlatMap(res0: Int, res1: String, res2: Boolean) = {
    import poly.identity

    val l = (23 :: "foo" :: HNil) :: HNil :: (true :: HNil) :: HNil

    l flatMap identity should be(res0 :: res1 :: res2 :: HNil)

  }

  /** It has a set of fully polymorphic fold operations which take a polymorphic binary function value. The fold is sensitive
    * to the static types of all of the elements of the `HList`. Given the earlier definition of size,
    * {{{
    * object addSize extends Poly2 {
    * implicit  def default[T](implicit st: shapelessex.size.Case.Aux[T, Int]) =
    * at[Int, T]{ (acc, t) => acc+size(t) }
    * }
    * }}}
    */
  def exerciseFold(res0: Int) = {

    val l = 23 :: "foo" :: (13, "wibble") :: HNil

    l.foldLeft(0)(addSize) should be(res0)

  }

  /** It also has a zipper for traversal and persistent update,
    */
  def exerciseZipper(res0: Int, res1: (String, Int), res2: Double, res3: Int, res4: Double) = {
    import syntax.zipper._

    val l = 1 :: "foo" :: 3.0 :: HNil
    l.toZipper.right.put(("wibble", 45)).reify should be(res0 :: res1 :: res2 :: HNil)

    l.toZipper.right.delete.reify should be(res3 :: res4 :: HNil)

  }

  import CovariantHelper._

  /** It is covariant,
    * {{{
    * object CovariantHelper {
    *
    * trait Fruit
    * case class Apple() extends Fruit
    * case class Pear() extends Fruit
    *
    * type FFFF = Fruit :: Fruit :: Fruit :: Fruit :: HNil
    * type APAP = Apple :: Pear :: Apple :: Pear :: HNil
    *
    * val a : Apple = Apple()
    * val p : Pear = Pear()
    *
    * val apap : APAP = a :: p :: a :: p :: HNil
    *
    * }
    * }}}
    */
  def exerciseCovariant(res0: Boolean) = {
    import scala.reflect.runtime.universe._

    implicitly[TypeTag[APAP]].tpe.typeConstructor <:< typeOf[FFFF] should be(res0)
  }

  /** And it has a unify operation which converts it to an HList of elements of the least upper bound of the original types,
    */
  def exerciseUnify(res0: Boolean, res1: Boolean) = {
    apap.isInstanceOf[FFFF] should be(res0)
    apap.unify.isInstanceOf[FFFF] should be(res1)
  }

  /** It supports conversion to an ordinary Scala `List` of elements of the least upper bound of the original types,
    */
  def exerciseConversionToList(res0: List[Fruit]) = {
    apap.toList should be(res0)
  }

  /** And it has a `Typeable` type class instance (see below), allowing, eg. vanilla `List[Any]`'s or `HList`'s with
    * elements of type `Any` to be safely cast to precisely typed `HList`'s.
    * These last three features make this `HList` dramatically more practically useful than `HList`'s are typically thought to be:
    * normally the full type information required to work with them is too fragile to cross subtyping or I/O boundaries.
    * This implementation supports the discarding of precise information where necessary.
    * (eg. to serialize a precisely typed record after construction), and its later reconstruction.
    * (eg. a weakly typed deserialized record with a known schema can have it's precise typing reestabilished).
    */
  def exerciseTypeable(res0: Option[APAP]) = {
    import syntax.typeable._

    val ffff: FFFF = apap.unify
    val precise: Option[APAP] = ffff.cast[APAP]

    precise should be(res0)

  }

}
