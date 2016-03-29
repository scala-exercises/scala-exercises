package catslib

import org.scalatest._

import cats._
import cats.std.all._
import cats.syntax.all._

/**
  * `Monoid` extends the `Semigroup` type class, adding an
  * `empty` method to semigroup's `combine`. The `empty` method must return a
  * value that when combined with any other instance of that type returns the
  * other instance, i.e.
  *
  * {{{
  * (combine(x, empty) == combine(empty, x) == x)
  * }}}
  *
  * For example, if we have a `Monoid[String]` with `combine` defined as string
  * concatenation, then `empty = ""`.
  *
  * Having an `empty` defined allows us to combine all the elements of some
  * potentially empty collection of `T` for which a `Monoid[T]` is defined and
  * return a `T`, rather than an `Option[T]` as we have a sensible default to
  * fall back to.
  *
  * @param name monoid
  */
object MonoidSection extends FlatSpec with Matchers with exercise.Section {
  /**
    * First some imports.
    *
    * {{{
    * import cats._
    * import cats.std.all._
    * import cats.syntax.all._
    * }}}
    *
    * And let's see the implicit instance of `Monoid[String]` in action.
    */
  def monoidEmpty(res0: String, res1: String, res2: String) = {
    import cats.implicits._

    Monoid[String].empty should be(res0)
    Monoid[String].combineAll(List("a", "b", "c")) should be(res1)
    Monoid[String].combineAll(List()) should be(res2)
  }

  /**
    * The advantage of using these type class provided methods, rather than the
    * specific ones for each type, is that we can compose monoids to allow us to
    * operate on more complex types, e.g.
    */
  def monoidAdvantage(res0: Map[String, Int], res1: Map[String, Int]) = {
    Monoid[Map[String,Int]].combineAll(List(Map("a" -> 1, "b" -> 2), Map("a" -> 3))) should be(res0)
    Monoid[Map[String,Int]].combineAll(List()) should be(res1)
  }

  /**
    * This is also true if we define our own instances. As an example, let's use
    * `Foldable`'s `foldMap`, which maps over values accumulating
    * the results, using the available `Monoid` for the type mapped onto.
    *
    */
  def monoidFoldmap(res0: Int, res1: String) = {
    val l = List(1, 2, 3, 4, 5)
    l.foldMap(identity) should be(res0)
    l.foldMap(i => i.toString) should be(res1)
  }

  /**
    * To use this
    * with a function that produces a tuple, we can define a `Monoid` for a tuple 
    * that will be valid for any tuple where the types it contains also have a 
    * `Monoid` available.
    *
    * This way we are able to combine both values in one pass, hurrah!
    */
  def tupleMonoid(res0: (Int, String)) = {
    implicit def tupleMonoid[A : Monoid, B : Monoid]: Monoid[(A, B)] =
       new Monoid[(A, B)] {
         def combine(x: (A, B), y: (A, B)): (A, B) = {
           val (xa, xb) = x
           val (ya, yb) = y
           (Monoid[A].combine(xa, ya), Monoid[B].combine(xb, yb))
         }
         def empty: (A, B) = (Monoid[A].empty, Monoid[B].empty)
       }

    val l = List(1, 2, 3, 4, 5)
    l.foldMap(i => (i, i.toString)) should be(res0)
  }
}
