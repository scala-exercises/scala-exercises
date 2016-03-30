package shapelessex

import org.scalatest._
import shapeless._
import poly.{ ~> }

/** == Polymorphic function values ==
  *
  * Ordinary [[http://www.chuusai.com/2012/04/27/shapeless-polymorphic-function-values-1/ Scala function values are monomorphic]]. shapeless, however, provides an encoding of polymorphic
  * function values. It supports [[http://en.wikipedia.org/wiki/Natural_transformation natural transformations]], which are familiar from libraries like Scalaz
  *
  * @param name polymorphic_function_values
  */
object PolyExercises extends FlatSpec with Matchers with exercise.Section {

  object choose extends (Set ~> Option) {
    def apply[T](s: Set[T]) = s.headOption
  }

  /** `choose` is a function from Sets to Options with no type specific cases
    * {{{
    * object choose extends (Set ~> Option) {
    * def apply[T](s : Set[T]) = s.headOption
    * }
    * }}}
    */
  def exerciseChoose(res0: Option[Int], res1: Option[Char]) = {
    import shapeless.poly._
    // choose is a function from Sets to Options with no type specific cases

    choose(Set(1, 2, 3)) should be(res0)
    choose(Set('a', 'b', 'c')) should be(res1)
  }

  /** Being polymorphic, they may be passed as arguments to functions or methods and then applied to values of different types
    * within those functions,
    *
    * {{{
    * scala> def pairApply(f: Set ~> Option) = (f(Set(1, 2, 3)), f(Set('a', 'b', 'c')))
    * pairApply: (f: shapeless.poly.~>[Set,Option])(Option[Int], Option[Char])
    * scala> pairApply(choose)
    * res2: (Option[Int], Option[Char]) = (Some(1),Some(a))
    * }}}
    */
  def exercisePairApply(res0: Option[Int], res1: Option[Char]) = {
    def pairApply(f: Set ~> Option) = (f(Set(1, 2, 3)), f(Set('a', 'b', 'c')))

    pairApply(choose) should be(res0, res1)
  }

  /** They are nevertheless interoperable with ordinary monomorphic function values.
    * `choose` is convertible to an ordinary monomorphic function value and can be
    * mapped across an ordinary Scala List
    */
  def exerciseMonomorphicChoose(res0: Option[Int], res1: Option[Int]) = {
    (List(Set(1, 3, 5), Set(2, 4, 6)) map choose) should be(List(res1, res0))
  }

  /** However, they are [[http://www.chuusai.com/2012/05/10/shapeless-polymorphic-function-values-2/ more general than natural transformations]] and are able to capture type-specific cases
    * which, as we'll see below, makes them ideal for generic programming,
    * `size` is a function from Ints or Strings or pairs to a `size` defined
    * by type specific cases
    */
  def exerciseSize(res0: Int, res1: Int, res2: Int, res3: Int) = {

    object size extends Poly1 {
      implicit def caseInt = at[Int](x ⇒ 1)
      implicit def caseString = at[String](_.length)
      implicit def caseTuple[T, U](implicit st: Case.Aux[T, Int], su: Case.Aux[U, Int]) =
        at[(T, U)](t ⇒ size(t._1) + size(t._2))
    }

    size(23) should be(res0)
    size("foo") should be(res1)
    size((23, "foo")) should be(res2)
    size(((23, "foo"), 13)) should be(res3)
  }

}
