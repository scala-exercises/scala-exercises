package catslib

import cats._
import org.scalatest._

/** Identity
  *
  * The identity monad can be seen as the ambient monad that encodes the
  * effect of having no effect. It is ambient in the sense that plain pure
  * values are values of <code>Id</code>.
  *
  * It is encoded as:
  *
  * {{{
  * type Id[A] = A
  * }}}
  *
  * That is to say that the type Id[A] is just a synonym for A.  We can
  * freely treat values of type <code>A</code> as values of type <code>Id[A]</code>, and
  * vice-versa.
  *
  * {{{
  * import cats._
  *
  * val x: Id[Int] = 1
  * val y: Int = x
  * }}}
  *
  */
object IdentitySection extends FlatSpec with Matchers with exercise.Section {

  /**
    * We can freely compare values of <code>Id[T]</code> with unadorned
    * values of type <code>T</code>.
    */
  def identityType(res0: Int) {
    val anId: Id[Int] = 42
    anId should be (res0)
  }

  /**
    * Using this type declaration, we can treat our Id type constructor as a
    * <code>Monad</code> and as a <code>Comonad</code>. The <code>pure</code>
    * method, which has type <code>A => Id[A]</code> just becomes the identity
    * function.  The <code>map</code> method from <code>Functor</code> just becomes function
    * application:
    * {{{
    * import cats.Functor
    *
    * val one: Int = 1
    * Functor[Id].map(one)(_ + 1)
    * }}}
    */
  def pureIdentity(res0: Int) = {
    Applicative[Id].pure(42) should be (res0)
  }

  /**
    * Compare the signatures of <code>map</code> and <code>flatMap</code> and <code>coflatMap</code>:
    *
    * {{{
    *   def map[A, B](fa: Id[A])(f: A => B): Id[B]
    *   def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B]
    *   def coflatMap[A, B](a: Id[A])(f: Id[A] => B): Id[B]
    * }}}
    *
    * You'll notice that in the flatMap signature, since <code>Id[B]</code> is the same
    * as <code>B</code> for all B, we can rewrite the type of the <code>f</code> parameter to be
    * <code>A => B</code> instead of <code>A => Id[B]</code>, and this makes the signatures of the
    * two functions the same, and, in fact, they can have the same
    * implementation, meaning that for <code>Id</code>, <code>flatMap</code> is also just function
    * application:
    *
    * {{{
    * import cats.Monad
    *
    * val one: Int = 1
    * Monad[Id].map(one)(_ + 1)
    * Monad[Id].flatMap(one)(_ + 1)
    * }}}
    */
  def idComonad(res0: Int) = {
    val fortytwo: Int = 42
    Comonad[Id].coflatMap(fortytwo)(_ + 1) should be (res0)
  }
}

