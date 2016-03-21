package catslib

import org.scalatest._

import cats._
import cats.std.option._
import cats.std.list._

/** A `Functor` is a ubiquitous type class involving types that have one
  * "hole", i.e. types which have the shape `F[?]`, such as `Option`,
  * `List` and `Future`. (This is in contrast to a type like `Int` which has
  * no hole, or `Tuple2` which has two holes (`Tuple2[?,?]`)).
  *
  * The `Functor` category involves a single operation, named `map`:
  *
  * {{{
  * def map[A, B](fa: F[A])(f: A => B): F[B]
  * }}}
  *
  * This method takes a function `A => B` and turns an `F[A]` into an
  * `F[B]`.  The name of the method `map` should remind you of the `map`
  * method that exists on many classes in the Scala standard library, for
  * example:
  *
  * {{{
  * Option(1).map(_ + 1)
  * List(1,2,3).map(_ + 1)
  * Vector(1,2,3).map(_.toString)
  * }}}
  *
  * = Creating Functor instances =
  *
  * We can trivially create a `Functor` instance for a type which has a well
  * behaved `map` method:
  *
  * {{{
  * import cats._
  *
  * implicit val optionFunctor: Functor[Option] = new Functor[Option] {
  *   def map[A,B](fa: Option[A])(f: A => B) = fa map f
  * }
  *
  * implicit val listFunctor: Functor[List] = new Functor[List] {
  *   def map[A,B](fa: List[A])(f: A => B) = fa map f
  * }
  * }}}
  *
  * However, functors can also be created for types which don't have a `map`
  * method. For example, if we create a `Functor` for `Function1[In, ?]`
  * we can use `andThen` to implement `map`:
  *
  * {{{
  * implicit def function1Functor[In]: Functor[Function1[In, ?]] =
  *   new Functor[Function1[In, ?]] {
  *     def map[A,B](fa: In => A)(f: A => B): Function1[In,B] = fa andThen f
  *   }
  * }}}
  *
  * This example demonstrates the use of the
  * [[https://github.com/non/kind-projector kind-projector compiler plugin]]
  * This compiler plugin can help us when we need to change the number of type
  * holes. In the example above, we took a type which normally has two type holes,
  * `Function1[?,?]` and constrained one of the holes to be the `In` type,
  * leaving just one hole for the return type, resulting in `Function1[In,?]`.
  * Without kind-projector, we'd have to write this as something like
  * `({type F[A] = Function1[In,A]})#F`, which is much harder to read and understand.
  *
  * @param name functor
  */
object FunctorSection extends FlatSpec with Matchers with exercise.Section {
  /**
    *
    * = Using Functor =
    *
    * == map ==
    *
    * `List` is a functor which applies the function to each element of the list:
    *
    * {{{
    * Functor[List].map(List("qwer", "adsfg"))(_.length)
    * }}}
    *
    * `Option` is a functor which only applies the function when the `Option` value
    * is a `Some`:
    *
    */
  def usingFunctor(res0: Option[Int], res1: Option[Int]) = {
    Functor[Option].map(Option("Hello"))(_.length) should be(res0)
    Functor[Option].map(None: Option[String])(_.length) should be(res1)
  }

  /**
    *
    * = Derived methods =
    *
    * == lift ==
    *
    * We can use `Functor` to "lift" a function from `A => B` to `F[A] => F[B]`:
    *
    * {{{
    * val lenOption: Option[String] => Option[Int] = Functor[Option].lift(_.length)
    * lenOption(Some("abcd"))
    * }}}
    *
    *  We can now apply the `lenOption` function to `Option` instances.
    *
    */
  def liftingToAFunctor(res0: Option[Int]) = {
    val lenOption: Option[String] => Option[Int] = Functor[Option].lift(_.length)
    lenOption(Some("Hello")) should be(res0)
  }

  /**
    * == fproduct ==
    *
    * `Functor` provides an `fproduct` function which pairs a value with the
    * result of applying a function to that value.
    *
    */
  def usingFproduct(res0: Int, res1: Int, res2: Int) = {
    val source = List("Cats", "is", "awesome")
    val product = Functor[List].fproduct(source)(_.length).toMap

    product.get("Cats").getOrElse(0) should be(res0)
    product.get("is").getOrElse(0) should be(res1)
    product.get("awesome").getOrElse(0) should be(res2)
  }

  /**
    *
    * == compose ==
    *
    * Functors compose! Given any functor `F[_]` and any functor `G[_]` we can
    * create a new functor `F[G[_]]` by composing them:
    *
    * {{{
    * val listOpt = Functor[List] compose Functor[Option]
    * }}}
    *
    * In the previous example the resulting functor will apply the `map` operation through the two
    * type constructors: `List` and `Option`.
    *
    */
  def composingFunctors(res0: List[Option[Int]]) = {
    val listOpt = Functor[List] compose Functor[Option]
    listOpt.map(List(Some(1), None, Some(3)))(_ + 1) should be(res0)
  }
}
