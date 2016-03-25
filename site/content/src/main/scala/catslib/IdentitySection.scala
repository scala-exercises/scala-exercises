package catslib

import cats._
import org.scalatest._

/** The identity monad can be seen as the ambient monad that encodes the
  * effect of having no effect. It is ambient in the sense that plain pure
  * values are values of `Id`.
  *
  * It is encoded as:
  *
  * {{{
  * type Id[A] = A
  * }}}
  *
  * That is to say that the type Id[A] is just a synonym for A.  We can
  * freely treat values of type `A` as values of type `Id[A]`, and
  * vice-versa.
  *
  * {{{
  * import cats._
  *
  * val x: Id[Int] = 1
  * val y: Int = x
  * }}}
  *
  * @param name identity
  */
object IdentitySection extends FlatSpec with Matchers with exercise.Section {

  /** We can freely compare values of `Id[T]` with unadorned
    * values of type `T`.
    */
  def identityType(res0: Int) {
    val anId: Id[Int] = 42
    anId should be (res0)
  }

  /** Using this type declaration, we can treat our Id type constructor as a
    * `Monad` and as a `Comonad`. The `pure`
    * method, which has type `A => Id[A]` just becomes the identity
    * function.  The `map` method from `Functor` just becomes function
    * application:
    *
    * {{{
    * import cats.Functor
    *
    * val one: Int = 1
    * Functor[Id].map(one)(_ + 1)
    * }}}
    *
    */
  def pureIdentity(res0: Int) = {
    Applicative[Id].pure(42) should be (res0)
  }

  /** Compare the signatures of `map` and `flatMap` and `coflatMap`:
    *
    * {{{
    *   def map[A, B](fa: Id[A])(f: A => B): Id[B]
    *   def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B]
    *   def coflatMap[A, B](a: Id[A])(f: Id[A] => B): Id[B]
    * }}}
    *
    * You'll notice that in the flatMap signature, since `Id[B]` is the same
    * as `B` for all B, we can rewrite the type of the `f` parameter to be
    * `A => B` instead of `A => Id[B]`, and this makes the signatures of the
    * two functions the same, and, in fact, they can have the same
    * implementation, meaning that for `Id`, `flatMap` is also just function
    * application:
    *
    * {{{
    * import cats.Monad
    *
    * val one: Int = 1
    * Monad[Id].map(one)(_ + 1)
    * Monad[Id].flatMap(one)(_ + 1)
    * }}}
    *
    */
  def idComonad(res0: Int) = {
    val fortytwo: Int = 42
    Comonad[Id].coflatMap(fortytwo)(_ + 1) should be (res0)
  }
}
