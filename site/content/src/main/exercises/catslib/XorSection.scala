package catslib

import org.scalatest._

import cats.data.Xor

object XorStyle {
  def parse(s: String): Xor[NumberFormatException, Int] =
    if (s.matches("-?[0-9]+")) Xor.right(s.toInt)
    else Xor.left(new NumberFormatException(s"${s} is not a valid integer."))

  def reciprocal(i: Int): Xor[IllegalArgumentException, Double] =
    if (i == 0) Xor.left(new IllegalArgumentException("Cannot take reciprocal of 0."))
    else Xor.right(1.0 / i)

  def stringify(d: Double): String = d.toString
}

/** Xor
  *
  * In day-to-day programming, it is fairly common to find ourselves writing functions that
  * can fail. For instance, querying a service may result in a connection issue, or some
  * unexpected JSON response.
  *
  * To communicate these errors it has become common practice to throw exceptions. However,
  * exceptions are not tracked in any way, shape, or form by the Scala compiler. To see
  * what kind of exceptions (if any) a function may throw, we have to dig through the source code.
  * Then to handle these exceptions, we have to make sure we catch them at the call site.
  * This all becomes even more unwieldy when we try to compose exception-throwing procedures.
  *
  * {{{
  * val throwsSomeStuff: Int => Double = ???
  *
  * val throwsOtherThings: Double => String = ???
  *
  * val moreThrowing: String => List[Char] = ???
  *
  * val magic = throwsSomeStuff.andThen(throwsOtherThings).andThen(moreThrowing)
  * }}}
  *
  * Assume we happily throw exceptions in our code. Looking at the types, any of those functions
  * can throw any number of exceptions, we don't know. When we compose, exceptions from any of
  * the constituent functions can be thrown. Moreover, they may throw the same kind of exception
  * (e.g. `IllegalArgumentException`) and thus it gets tricky tracking exactly where that
  * exception came from.
  *
  * How then do we communicate an error? By making it explicit in the data type we return.
  *
  * =`Xor` vs `Validated`=
  *
  * In general, `Validated` is used to accumulate errors, while `Xor` is used to short-circuit a computation upon the first error. For more information, see the `Validated` vs `Xor` section of the `Validated` documentation.
  *
  * =Why not `Either`=
  *
  * `Xor` is very similar to `scala.util.Either` - in fact, they are *isomorphic* (that is,
  * any `Either` value can be rewritten as an `Xor` value, and vice versa).
  *
  * {{{
  * sealed abstract class Xor[+A, +B]
  *
  * object Xor {
  *   final case class Left[+A](a: A) extends Xor[A, Nothing]
  *   final case class Right[+B](b: B) extends Xor[Nothing, B]
  * }
  * }}}
  *
  * Just like `Either`, it has two type parameters. Instances of `Xor` either hold a value
  * of one type parameter, or the other. Why then does it exist at all?
  *
  * Taking a look at `Either`, we notice it lacks `flatMap` and `map` methods. In order to map
  * over an `Either[A, B]` value, we have to state which side we want to map over. For example,
  * if we want to map `Either[A, B]` to `Either[A, C]` we would need to map over the right side.
  * This can be accomplished by using the `Either#right` method, which returns a `RightProjection`
  * instance. `RightProjection` does have `flatMap` and `map` on it, which acts on the right side
  * and ignores the left - this property is referred to as "right-bias."
  *
  * {{{
  * val e1: Either[String, Int] = Right(5)
  * e1.right.map(_ + 1)
  *
  * val e2: Either[String, Int] = Left("hello")
  * e2.right.map(_ + 1)
  * }}}
  *
  * Note the return types are themselves back to `Either`, so if we want to make more calls to
  * `flatMap` or `map` then we again must call `right` or `left`.
  *
  *
  */
object XorSection extends FlatSpec with Matchers with exercise.Section {
  /**
    * More often than not we want to just bias towards one side and call it a day - by convention,
    * the right side is most often chosen. This is the primary difference between `Xor` and `Either` -
    * `Xor` is right-biased. `Xor` also has some more convenient methods on it, but the most
    * crucial one is the right-biased being built-in.
    *
    */
  def xorMapRightBias(res0: Int, res1: String) = {
    import cats.data.Xor

    val right: String Xor Int = Xor.right(5)
    right.map(_ + 1) should be(Xor.right(res0))

    val left: String Xor Int = Xor.left("Something went wrong")
    left.map(_ + 1) should be(Xor.left(res1))
  }

  /**
    * Because `Xor` is right-biased, it is possible to define a `Monad` instance for it. You
    * could also define one for `Either` but due to how it's encoded it may seem strange to fix a
    * bias direction despite it intending to be flexible in that regard. The `Monad` instance for
    * `Xor` is consistent with the behavior of the data type itself, whereas the one for `Either`
    * would only introduce bias when `Either` is used in a generic context (a function abstracted
    * over `M[_] : Monad`).
    *
    * Since we only ever want the computation to continue in the case of `Xor.Right` (as captured
    * by the right-bias nature), we fix the left type parameter and leave the right one free.
    *
    * {{{
    * import cats.Monad
    *
    * implicit def xorMonad[Err]: Monad[Xor[Err, ?]] =
    *   new Monad[Xor[Err, ?]] {
    *     def flatMap[A, B](fa: Xor[Err, A])(f: A => Xor[Err, B]): Xor[Err, B] =
    *       fa.flatMap(f)
    *
    *     def pure[A](x: A): Xor[Err, A] = Xor.right(x)
    *   }
    * }}}
    *
    */
  def xorMonad(res0: Int, res1: String) = {
    import cats.data.Xor

    val right: String Xor Int = Xor.right(5)
    right.flatMap(x => Xor.right(x + 1)) should be(Xor.right(res0))

    val left: String Xor Int = Xor.left("Something went wrong")
    left.flatMap(x => Xor.right(x + 1)) should be(Xor.left(res1))
  }

  /**
    * = Using `Xor` instead of exceptions =
    *
    * As a running example, we will have a series of functions that will parse a string into an integer,
    * take the reciprocal, and then turn the reciprocal into a string.
    *
    * In exception-throwing code, we would have something like this:
    *
    * {{{
    * object ExceptionStyle {
    *   def parse(s: String): Int =
    *     if (s.matches("-?[0-9]+")) s.toInt
    *     else throw new NumberFormatException(s"${s} is not a valid integer.")
    *
    *   def reciprocal(i: Int): Double =
    *     if (i == 0) throw new IllegalArgumentException("Cannot take reciprocal of 0.")
    *     else 1.0 / i
    *
    *   def stringify(d: Double): String = d.toString
    * }
    * }}}
    *
    * Instead, let's make the fact that some of our functions can fail explicit in the return type.
    *
    * {{{
    * object XorStyle {
    *   def parse(s: String): Xor[NumberFormatException, Int] =
    *     if (s.matches("-?[0-9]+")) Xor.right(s.toInt)
    *     else Xor.left(new NumberFormatException(s"${s} is not a valid integer."))
    *
    *   def reciprocal(i: Int): Xor[IllegalArgumentException, Double] =
    *     if (i == 0) Xor.left(new IllegalArgumentException("Cannot take reciprocal of 0."))
    *     else Xor.right(1.0 / i)
    *
    *   def stringify(d: Double): String = d.toString
    * }
    * }}}
    *
    */
  def xorStyleParse(res0: Boolean, res1: Boolean) = {
    XorStyle.parse("Not a number").isRight should be(res0)
    XorStyle.parse("2").isRight should be(res1)
  }

  /**
    * Now, using combinators like `flatMap` and `map`, we can compose our functions together.
    *
    * {{{
    * import XorStyle._
    *
    * def magic(s: String): Xor[Exception, String] =
    *   parse(s).flatMap(reciprocal).map(stringify)
    * }}}
    *
    */
  def xorComposition(res0: Boolean, res1: Boolean, res2: Boolean) = {
    import XorStyle._

    def magic(s: String): Xor[Exception, String] =
      parse(s).flatMap(reciprocal).map(stringify)

    magic("0").isRight should be (res0)
    magic("1").isRight should be (res1)
    magic("Not a number").isRight should be (res2)
  }
}
