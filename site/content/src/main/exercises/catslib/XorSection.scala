package catslib

import org.scalatest._

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
  * ===`Xor` vs `Validated`===
  *
  * In general, `Validated` is used to accumulate errors, while `Xor` is used to short-circuit a computation upon the first error. For more information, see the `Validated</code> vs `Xor` section of the `Validated` documentation.
  *
  * ===Why not `Either`===
  *
  * `Xor` is very similar to `scala.util.Either</code> - in fact, they are *isomorphic* (that is,
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
  * Taking a look at `Either`, we notice it lacks `flatMap</code> and `map</code> methods. In order to map
  * over an `Either[A, B]</code> value, we have to state which side we want to map over. For example,
  * if we want to map `Either[A, B]</code> to `Either[A, C]</code> we would need to map over the right side.
  * This can be accomplished by using the `Either#right</code> method, which returns a `RightProjection</code>
  * instance. `RightProjection</code> does have `flatMap</code> and `map</code> on it, which acts on the right side
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
  * `flatMap</code> or `map</code> then we again must call `right</code> or `left`.
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
}
