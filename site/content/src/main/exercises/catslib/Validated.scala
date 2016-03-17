package catslib

import org.scalatest._

import cats.data.Validated
import cats.data.NonEmptyList
import cats.data.Xor

import ValidatedHelpers._

/**
  * Imagine you are filling out a web form to signup for an account. You input your username and password and submit.
  * Response comes back saying your username can't have dashes in it, so you make some changes and resubmit. Can't
  * have special characters either. Change, resubmit. Passwords need to have at least one capital letter. Change,
  * resubmit. Password needs to have at least one number.
  *
  * Or perhaps you're reading from a configuration file. One could imagine the configuration library you're using returns
  * a `scala.util.Try`, or maybe a `scala.util.Either` (or `cats.data.Xor`). Your parsing may look something like:
  *
  * {{{
  * case class ConnectionParams(url: String, port: Int)
  *
  * for {
  *   url  <- config[String]("url")
  *   port <- config[Int]("port")
  * } yield ConnectionParams(url, port)
  * }}}
  *
  * You run your program and it says key "url" not found, turns out the key was "endpoint". So you change your code
  * and re-run. Now it says the "port" key was not a well-formed integer.
  *
  * It would be nice to have all of these errors be reported simultaneously. That the username can't have dashes can
  * be validated separately from it not having special characters, as well as from the password needing to have certain
  * requirements. A misspelled (or missing) field in a config can be validated separately from another field not being
  * well-formed.
  *
  * Enter `Validated`.
  *
  * ## Parallel validation ##
  *
  * Our goal is to report any and all errors across independent bits of data. For instance, when we ask for several
  * pieces of configuration, each configuration field can be validated separately from one another. How then do we
  * enforce that the data we are working with is independent? We ask for both of them up front.
  *
  * As our running example, we will look at config parsing. Our config will be represented by a
  * `Map[String, String]`. Parsing will be handled by a `Read` type class - we provide instances
  * just for `String` and `Int` for brevity.
  *
  * {{{
  * trait Read[A] {
  *   def read(s: String): Option[A]
  * }
  *
  * object Read {
  *   def apply[A](implicit A: Read[A]): Read[A] = A
  *
  *   implicit val stringRead: Read[String] =
  *     new Read[String] { def read(s: String): Option[String] = Some(s) }
  *
  *   implicit val intRead: Read[Int] =
  *     new Read[Int] {
  *       def read(s: String): Option[Int] =
  *         if (s.matches("-?[0-9]+")) Some(s.toInt)
  *         else None
  *     }
  * }
  * }}}
  *
  * Then we enumerate our errors - when asking for a config value, one of two things can
  * go wrong: the field is missing, or it is not well-formed with regards to the expected
  * type.
  *
  * {{{
  * sealed abstract class ConfigError
  * final case class MissingConfig(field: String) extends ConfigError
  * final case class ParseError(field: String) extends ConfigError
  * }}}
  *
  * We need a data type that can represent either a successful value (a parsed configuration),
  * or an error. It'd look like in the following example, which cats provides in `cats.data.Validated`.
  *
  * {{{
  * sealed abstract class Validated[+E, +A]
  *
  * object Validated {
  *   final case class Valid[+A](a: A) extends Validated[Nothing, A]
  *   final case class Invalid[+E](e: E) extends Validated[E, Nothing]
  * }
  * }}}
  *
  * Now we are ready to write our parser.
  *
  * {{{
  * import cats.data.Validated
  * import cats.data.Validated.{Invalid, Valid}
  *
  * case class Config(map: Map[String, String]) {
  *   def parse[A : Read](key: String): Validated[ConfigError, A] =
  *     map.get(key) match {
  *       case None        => Invalid(MissingConfig(key))
  *       case Some(value) =>
  *         Read[A].read(value) match {
  *           case None    => Invalid(ParseError(key))
  *           case Some(a) => Valid(a)
  *         }
  *     }
  * }
  * }}}

  * Everything is in place to write the parallel validator. Recall that we can only do parallel
  * validation if each piece is independent. How do we enforce the data is independent? By asking
  * for all of it up front. Let's start with two pieces of data.
  *
  * {{{
  * def parallelValidate[E, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
  *   (v1, v2) match {
  *     case (Valid(a), Valid(b))       => Valid(f(a, b))
  *     case (Valid(_), i@Invalid(_))   => i
  *     case (i@Invalid(_), Valid(_))   => i
  *     case (Invalid(e1), Invalid(e2)) => ???
  *   }
  * }}}
  *
  * We've run into a problem. In the case where both have errors, we want to report both. But we have
  * no way of combining the two errors into one error! Perhaps we can put both errors into a `List`,
  * but that seems needlessly specific - clients may want to define their own way of combining errors.
  *
  * How then do we abstract over a binary operation? The `Semigroup` type class captures this idea.
  *
  * {{{
  * import cats.Semigroup
  *
  * def parallelValidate[E : Semigroup, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
  *   (v1, v2) match {
  *     case (Valid(a), Valid(b))       => Valid(f(a, b))
  *     case (Valid(_), i@Invalid(_))   => i
  *     case (i@Invalid(_), Valid(_))   => i
  *     case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
  *   }
  * }}}
  *
  * Perfect! But.. going back to our example, we don't have a way to combine `ConfigError`s. But as clients,
  * we can change our `Validated` values where the error can be combined, say, a `List[ConfigError]`. It is
  * more common however to use a `NonEmptyList[ConfigError]` - the `NonEmptyList` statically guarantees we
  * have at least one value, which aligns with the fact that if we have an `Invalid`, then we most
  * certainly have at least one error. This technique is so common there is a convenient method on `Validated`
  * called `toValidatedNel` that turns any `Validated[E, A]` value to a `Validated[NonEmptyList[E], A]`.
  * Additionally, the type alias `ValidatedNel[E, A]` is provided.
  *
  * Time to parse.
  *
  * {{{
  * import cats.SemigroupK
  * import cats.data.NonEmptyList
  * import cats.std.list._
  *
  * implicit val nelSemigroup: Semigroup[NonEmptyList[ConfigError]] =
  *   SemigroupK[NonEmptyList].algebra[ConfigError]
  *
  * implicit val readString: Read[String] = Read.stringRead
  * implicit val readInt: Read[Int] = Read.intRead
  * }}}
  *
  * @param name validated
  */
object ValidatedSection extends FlatSpec with Matchers with exercise.Section {
  /**
    * When no errors are present in the configuration, we get a `ConnectionParams` wrapped in a `Valid` instance.
    */
  def noErrors(res0: Boolean, res1: String, res2: Int) = {
    val config = Config(Map(("url", "127.0.0.1"), ("port", "1337")))

    val valid = parallelValidate(
      config.parse[String]("url").toValidatedNel,
      config.parse[Int]("port").toValidatedNel
    )(ConnectionParams.apply)

    valid.isValid should be(res0)
    valid.getOrElse(ConnectionParams("", 0)) should be(ConnectionParams(res1, res2))
  }

  /**
    * But what happens when having one or more errors? They are accumulated in a `NonEmptyList`
    * wrapped in a `Invalid` instance.
    */
  def someErrors(res0: Boolean, res1: Boolean) = {
    val config = Config(Map(("endpoint", "127.0.0.1"), ("port", "not a number")))

    val invalid = parallelValidate(
      config.parse[String]("url").toValidatedNel,
      config.parse[Int]("port").toValidatedNel
    )(ConnectionParams.apply)

    import cats.data.Validated
    import cats.data.NonEmptyList

    invalid.isValid should be(res0)
    val errors = NonEmptyList(MissingConfig("url"), ParseError("port"))
    invalid == Validated.invalid(errors) should be(res1)
  }

  /**
    *
    * ## Apply ##
    *
    * Our `parallelValidate` function looks awfully like the `Apply#map2` function.
    *
    * {{{
    * def map2[F[_], A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]
    * }}}
    *
    * Which can be defined in terms of `Apply#ap` and `Apply#map`, the very functions needed to create an `Apply` instance.
    *
    * Can we perhaps define an `Apply` instance for `Validated`? Better yet, can we define an `Applicative` instance?
    *
    * {{{
    * import cats.Applicative
    *
    * implicit def validatedApplicative[E : Semigroup]: Applicative[Validated[E, ?]] =
    *   new Applicative[Validated[E, ?]] {
    *     def ap[A, B](f: Validated[E, A => B])(fa: Validated[E, A]): Validated[E, B] =
    *       (fa, f) match {
    *         case (Valid(a), Valid(fab)) => Valid(fab(a))
    *         case (i@Invalid(_), Valid(_)) => i
    *         case (Valid(_), i@Invalid(_)) => i
    *         case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
    *       }
    *
    *     def pure[A](x: A): Validated[E, A] = Validated.valid(x)
    *     def map[A, B](fa: Validated[E, A])(f: A => B): Validated[E, B] = fa.map(f)
    *     def product[A, B](fa: Validated[E, A], fb: Validated[E, B]): Validated[E, (A, B)] =
    *       ap(fa.map(a => (b: B) => (a, b)))(fb)
    *   }
    * }}}
    *
    * Awesome! And now we also get access to all the goodness of `Applicative`, which includes `map{2-22}`, as well as the
    * `Cartesian` syntax `|@|`.
    *
    * We can now easily ask for several bits of configuration and get any and all errors returned back.
    *
    * {{{
    * import cats.Apply
    * import cats.data.ValidatedNel
    *
    * implicit val nelSemigroup: Semigroup[NonEmptyList[ConfigError]] =
    *   SemigroupK[NonEmptyList].algebra[ConfigError]
    *
    * val config = Config(Map(("name", "cat"), ("age", "not a number"), ("houseNumber", "1234"), ("lane", "feline street")))
    *
    * case class Address(houseNumber: Int, street: String)
    * case class Person(name: String, age: Int, address: Address)
    * }}}
    *
    * Thus.
    *
    * {{{
    * val personFromConfig: ValidatedNel[ConfigError, Person] =
    *   Apply[ValidatedNel[ConfigError, ?]].map4(config.parse[String]("name").toValidatedNel,
    *                                            config.parse[Int]("age").toValidatedNel,
    *                                            config.parse[Int]("house_number").toValidatedNel,
    *                                            config.parse[String]("street").toValidatedNel) {
    *     case (name, age, houseNumber, street) => Person(name, age, Address(houseNumber, street))
    *   }
    *
    * We can now rewrite validations in terms of `Apply`.
    *
    * ## Of `flatMap`s and `Xor`s ##
    *
    * `Option` has `flatMap`, `Xor` has `flatMap`, where's `Validated`'s? Let's try to implement it - better yet,
    * let's implement the `Monad` type class.
    *
    * {{{
    * import cats.Monad
    *
    * implicit def validatedMonad[E]: Monad[Validated[E, ?]] =
    *   new Monad[Validated[E, ?]] {
    *     def flatMap[A, B](fa: Validated[E, A])(f: A => Validated[E, B]): Validated[E, B] =
    *       fa match {
    *         case Valid(a)     => f(a)
    *         case i@Invalid(_) => i
    *       }
    *
    *     def pure[A](x: A): Validated[E, A] = Valid(x)
    *   }
    * }}}
    *
    * Note that all `Monad` instances are also `Applicative` instances, where `ap` is defined as
    *
    * {{{
    * trait Monad[F[_]] {
    *   def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
    *   def pure[A](x: A): F[A]
    *
    *   def map[A, B](fa: F[A])(f: A => B): F[B] =
    *     flatMap(fa)(f.andThen(pure))
    *
    *   def ap[A, B](fa: F[A])(f: F[A => B]): F[B] =
    *     flatMap(fa)(a => map(f)(fab => fab(a)))
    * }
    * }}}
    *
    * However, the `ap` behavior defined in terms of `flatMap` does not behave the same as that of
    * our `ap` defined above. Observe:
    *
    * {{{
    * val v = validatedMonad.tuple2(Validated.invalidNel[String, Int]("oops"), Validated.invalidNel[String, Double]("uh oh"))
    * }}}
    *
    * This one short circuits! Therefore, if we were to define a `Monad` (or `FlatMap`) instance for `Validated` we would
    * have to override `ap` to get the behavior we want. But then the behavior of `flatMap` would be inconsistent with
    * that of `ap`, not good. Therefore, `Validated` has only an `Applicative` instance.
    *
    * ## Sequential Validation ##
    *
    * If you do want error accumulation but occasionally run into places where you sequential validation is needed, then `Validated` provides a couple methods that may be helpful.
    *
    * ### `andThen` ###
    *
    * The `andThen` method is similar to `flatMap` (such as `Xor.flatMap`). In the cause of success, it passes the valid value into a function that returns a new `Validated` instance.
    *
    * {{{
    * val houseNumber = config.parse[Int]("house_number").andThen{ n =>
    * if (n >= 0) Validated.valid(n)
    * else Validated.invalid(ParseError("house_number"))
    * }
    * }}}
    */
  def sequentialValidation(res0: Boolean, res1: Boolean) = {
    val config = Config(Map("house_number" -> "-42"))

    val houseNumber = config.parse[Int]("house_number").andThen{ n =>
      if (n >= 0) Validated.valid(n)
      else Validated.invalid(ParseError("house_number"))
    }

    houseNumber.isValid should be(res0)
    val error = ParseError("house_number")
    houseNumber == Validated.invalid(error) should be(res1)
  }

  /**
    * ### `withXor` ###
    *
    * The `withXor` method allows you to temporarily turn a `Validated` instance into an `Xor` instance and apply it to a function.
    *
    * {{{
    * import cats.data.Xor
    *
    * def positive(field: String, i: Int): ConfigError Xor Int = {
    *   if (i >= 0) Xor.right(i)
    *   else Xor.left(ParseError(field))
    * }
    * }}}
    *
    * So we can get `Xor`'s short-circuiting behaviour when using the `Validated` type.
    *
    */
  def validatedAsXor(res0: Boolean, res1: Boolean) = {
    val config = Config(Map("house_number" -> "-42"))

    val houseNumber = config.parse[Int]("house_number").withXor{ xor: ConfigError Xor Int =>
      xor.flatMap{ i =>
        positive("house_number", i)
      }
    }

    houseNumber.isValid should be(res0)
    val error = ParseError("house_number")
    houseNumber == Validated.invalid(error) should be(res1)
  }

}
