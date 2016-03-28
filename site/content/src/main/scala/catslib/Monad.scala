package catslib

import org.scalatest._

import cats._
import MonadHelpers._

/**
  * `Monad` extends the `Applicative` type class with a
  * new function `flatten`. Flatten takes a value in a nested context (eg.
  * `F[F[A]]` where F is the context) and "joins" the contexts together so
  * that we have a single context (ie. `F[A]`).
  *
  * @param name monad
  */
object MonadSection extends FlatSpec with Matchers with exercise.Section {
  /**
    * The name `flatten` should remind you of the functions of the same name on many
    * classes in the standard library.
    */
  def flattenRecap(res0: Option[Int], res1: Option[Int], res2: List[Int]) = {
    Option(Option(1)).flatten should be(res0)
    Option(None).flatten should be(res1)
    List(List(1),List(2,3)).flatten should be(res2)
  }

  /**
    *
    * = Monad instances =
    *
    * If `Applicative` is already present and `flatten` is well-behaved,
    * extending the `Applicative` to a `Monad` is trivial. To provide evidence
    * that a type belongs in the `Monad` type class, cats' implementation
    * requires us to provide an implementation of `pure` (which can be reused
    * from `Applicative`) and `flatMap`.
    *
    * We can use `flatten` to define `flatMap`: `flatMap` is just `map`
    * followed by `flatten`. Conversely, `flatten` is just `flatMap` using
    * the identity function `x => x` (i.e. `flatMap(_)(x => x)`).
    *
    * {{{
    * import cats._
    *
    * implicit def optionMonad(implicit app: Applicative[Option]) =
    *   new Monad[Option] {
    *     // Define flatMap using Option's flatten method
    *     override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] =
    *       app.map(fa)(f).flatten
    *     // Reuse this definition from Applicative.
    *     override def pure[A](a: A): Option[A] = app.pure(a)
    *   }
    * }}}
    *
    * Cats already provides a `Monad` instance of `Option`.
    *
    */
  def monadInstances(res0: Option[Int]) = {
    import cats._
    import cats.std.option._

    Monad[Option].pure(42) should be(res0)
  }

  /**
    * = flatMap =
    *
    * `flatMap` is often considered to be the core function of `Monad`, and cats
    * follows this tradition by providing implementations of `flatten` and `map`
    * derived from `flatMap` and `pure`.
    *
    * {{{
    * implicit val listMonad = new Monad[List] {
    *   def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] = fa.flatMap(f)
    *   def pure[A](a: A): List[A] = List(a)
    * }
    * }}}
    *
    * Part of the reason for this is that name `flatMap` has special significance in
    * scala, as for-comprehensions rely on this method to chain together operations
    * in a monadic context.
    */
  def monadFlatmap(res0: List[Int]) = {
    import cats._
    import cats.std.list._

    Monad[List].pure(42) should be(res0)
  }

  /**
    * = ifM =
    *
    * `Monad` provides the ability to choose later operations in a sequence based on
    * the results of earlier ones. This is embodied in `ifM`, which lifts an `if`
    * statement into the monadic context.
    */
  def monadIfm(res0: Option[String], res1: List[Int]) = {
    import cats._
    import cats.std.all._

    Monad[Option].ifM(Option(true))(Option("truthy"), Option("falsy")) should be(res0)
    Monad[List].ifM(List(true, false, true))(List(1, 2), List(3, 4)) should be(res1)
  }

  /**
    * = Composition =
    *
    * Unlike `Functor`s and `Applicative`s
    * not all `Monad`s compose. This means that even if `M[_]` and `N[_]` are
    * both `Monad`s, `M[N[_]]` is not guaranteed to be a `Monad`.
    *
    * However, many common cases do. One way of expressing this is to provide
    * instructions on how to compose any outer monad (`F` in the following
    * example) with a specific inner monad (`Option` in the following
    * example).
    *
    * {{{
    * case class OptionT[F[_], A](value: F[Option[A]])
    *
    * implicit def optionTMonad[F[_]](implicit F : Monad[F]) = {
    *   new Monad[OptionT[F, ?]] {
    *     def pure[A](a: A): OptionT[F, A] = OptionT(F.pure(Some(a)))
    *     def flatMap[A, B](fa: OptionT[F, A])(f: A => OptionT[F, B]): OptionT[F, B] =
    *       OptionT {
    *         F.flatMap(fa.value) {
    *           case None => F.pure(None)
    *           case Some(a) => f(a).value
    *         }
    *       }
    *   }
    * }
    * }}}
    *
    * This sort of construction is called a monad transformer. Cats already provides
    * a monad transformer for `Option` called `OptionT`.
    *
    */
  def monadComposition(res0: List[Option[Int]]) = {
    import cats.std.list._

    optionTMonad[List].pure(42) should be(OptionT(res0))
  }
}
