package catslib

import org.scalatest._

import cats._
import cats.implicits._

/**
  * Foldable type class instances can be defined for data structures that can be
  * folded to a summary value.
  *
  * In the case of a collection (such as `List` or `Set`), these methods will fold
  * together (combine) the values contained in the collection to produce a single
  * result. Most collection types have `foldLeft` methods, which will usually be
  * used by the associated `Foldable[_]` instance.
  *
  * `Foldable[F]` is implemented in terms of two basic methods:
  *
  *  - `foldLeft(fa, b)(f)` eagerly folds `fa` from left-to-right.
  *  - `foldRight(fa, b)(f)` lazily folds `fa` from right-to-left.
  *
  *
  * These form the basis for many other operations, see also:
  * [[https://www.cs.nott.ac.uk/~gmh/fold.pdf A tutorial on the universality and expressiveness of fold]]
  *
  * First some standard imports.
  *
  * {{{
  * import cats._
  * import cats.implicits._
  * }}}
  *
  * Apart from the familiar `foldLeft` and `foldRight`, `Foldable` has a number of other useful functions.
  *
  * @param name foldable
  */
object FoldableSection extends FlatSpec with Matchers with exercise.Section {
  /**
    * ## foldLeft ##
    *
    * `foldLeft` is an eager left-associative fold on `F` using the given function.
    *
    */
  def foldableFoldLeft(res0: Int, res1: String) = {
    Foldable[List].foldLeft(List(1, 2, 3), 0)(_ + _) should be(res0)
    Foldable[List].foldLeft(List("a", "b", "c"), "")(_ + _) should be(res1)
  }

  /**
    * ## foldRight ##
    *
    * `foldRight` is a lazy right-associative fold on `F` using the given function.
    * The function has the signature `(A, Eval[B]) => Eval[B]` to support laziness in
    * a stack-safe way.
    *
    */
  def foldableFoldRight(res0: Int) = {
    val lazyResult = Foldable[List].foldRight(List(1, 2, 3), Now(0))((x, rest) => Later(x + rest.value))
    lazyResult.value should be(res0)
  }

  /**
    * ## fold ##
    *
    * `fold`, also called `combineAll`, combines every value in the foldable using the fiven `Monoid` instance.
    *
    */
  def foldableFold(res0: String, res1: Int) = {
    Foldable[List].fold(List("a", "b", "c")) should be(res0)
    Foldable[List].fold(List(1, 2, 3)) should be(res1) // Hint: the implicit monoid for `Int` is the Sum monoid
  }

  /**
    * ## foldMap ##
    *
    * `foldMap` is similar to `fold` but maps every `A` value into `B` and then
    * combines them using the given `Monoid[B]` instance.
    *
    */
  def foldableFoldMap(res0: Int, res1: String) = {
    Foldable[List].foldMap(List("a", "b", "c"))(_.length) should be(res0)
    Foldable[List].foldMap(List(1, 2, 3))(_.toString) should be(res1)
  }

  /**
    * ## foldK ##
    *
    * `foldK` is similar to `fold` but combines every value in the foldable using the fiven `MonoidK[G]` instance
    * instead of `Monoid[G]`.
    */
  def foldableFoldk(res0: List[Int], res1: Option[String]) = {
    Foldable[List].foldK(List(List(1, 2), List(3, 4, 5))) should be(res0)
    Foldable[List].foldK(List(None, Option("two"), Option("three"))) should be(res1)
  }

  /**
    * ## find ##
    *
    * `find` searches for the first element matching the predicate, if one exists.
    */
  def foldableFind(res0: Option[Int], res1: Option[Int]) = {
    Foldable[List].find(List(1, 2, 3))(_ > 2) should be(res0)
    Foldable[List].find(List(1, 2, 3))(_ > 5) should be(res1)
  }

  /**
    * ## exists ##
    *
    * `exists` checks whether at least one element satisfies the predicate.
    */
  def foldableExists(res0: Boolean, res1: Boolean) = {
    Foldable[List].exists(List(1, 2, 3))(_ > 2) should be(res0)
    Foldable[List].exists(List(1, 2, 3))(_ > 5) should be(res1)
  }

  /**
    * ## forall ##
    *
    * `forall` checks whether all elements satisfy the predicate.
    */
  def foldableForall(res0: Boolean, res1: Boolean) = {
    Foldable[List].forall(List(1, 2, 3))(_ <= 3) should be(res0)
    Foldable[List].forall(List(1, 2, 3))(_ < 3) should be(res1)
  }

  /**
    * ## toList ##
    *
    * Convert `F[A]` to `List[A]`.
    */
  def foldableTolist(res0: List[Int], res1: List[Int], res2: List[Int]) = {
    Foldable[List].toList(List(1, 2, 3)) should be(res0)
    Foldable[Option].toList(Option(42)) should be(res1)
    Foldable[Option].toList(None) should be(res2)
  }

  /**
    * ## filter_ ##
    *
    * Convert `F[A]` to `List[A]` only including the elements that match a predicate.
    */
  def foldableFilter(res0: List[Int], res1: List[Int]) = {
    Foldable[List].filter_(List(1, 2, 3))(_ < 3) should be(res0)
    Foldable[Option].filter_(Option(42))(_ != 42) should be(res1)
  }

  /**
    * ## traverse_ ##
    *
    * `traverse` the foldable mapping `A` values to `G[B]`, and combining
    * them using `Applicative[G]` and discarding the results.
    *
    * This method is primarily useful when `G[_]` represents an action
    * or effect, and the specific `A` aspect of `G[A]` is not otherwise
    * needed. The `A` will be discarded and `Unit` returned instead.
    *
    */
  def foldableTraverse(res0: Option[Unit], res1: Option[Unit]) = {
    import cats.std.all._
    import cats.data.Xor

    def parseInt(s: String): Option[Int] =
      Xor.catchOnly[NumberFormatException](s.toInt).toOption

    Foldable[List].traverse_(List("1", "2", "3"))(parseInt) should be(res0)
    Foldable[List].traverse_(List("a", "b", "c"))(parseInt) should be(res1)
  }

  /**
    * ## compose ##
    *
    * We can compose `Foldable[F[_]]` and `Foldable[G[_]]` instances to obtain `Foldable[F[G]]`.
    */
  def foldableCompose(res0: Int, res1: String) = {
    val FoldableListOption = Foldable[List].compose[Option]
    FoldableListOption.fold(List(Option(1), Option(2), Option(3), Option(4))) should be(res0)
    FoldableListOption.fold(List(Option("1"), Option("2"), None, Option("3"))) should be(res1)
  }

  /**
    * # Foldable #
    *
    * Hence when defining some new data structure, if we can define a `foldLeft` and
    * `foldRight` we are able to provide many other useful operations, if not always
    * the most efficient implementations, over the structure without further
    * implementation.
    *
    * There are a few more methods that we haven't talked about but you probably can
    * guess what they do:
    *
    */
  def foldableMethods(res0: Boolean, res1: List[Int], res2: List[Int]) = {
    Foldable[List].isEmpty(List(1, 2, 3)) should be(res0)
    Foldable[List].dropWhile_(List(1, 2, 3))(_ < 2) should be(res1)
    Foldable[List].takeWhile_(List(1, 2, 3))(_ < 2) should be(res2)
  }


}
