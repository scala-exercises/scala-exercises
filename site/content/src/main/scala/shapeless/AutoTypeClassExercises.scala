package shapelessex

import org.scalatest._
import shapeless._

trait Monoid[T] {
  def zero: T
  def append(a: T, b: T): T
}

object Monoid extends ProductTypeClassCompanion[Monoid] {
  def mzero[T](implicit mt: Monoid[T]) = mt.zero

  implicit def booleanMonoid: Monoid[Boolean] = new Monoid[Boolean] {
    def zero = false
    def append(a: Boolean, b: Boolean) = a || b
  }

  implicit def intMonoid: Monoid[Int] = new Monoid[Int] {
    def zero = 0
    def append(a: Int, b: Int) = a + b
  }

  implicit def doubleMonoid: Monoid[Double] = new Monoid[Double] {
    def zero = 0.0
    def append(a: Double, b: Double) = a + b
  }

  implicit def stringMonoid: Monoid[String] = new Monoid[String] {
    def zero = ""
    def append(a: String, b: String) = a + b
  }

  object typeClass extends ProductTypeClass[Monoid] {
    def emptyProduct = new Monoid[HNil] {
      def zero = HNil
      def append(a: HNil, b: HNil) = HNil
    }

    def product[F, T <: HList](mh: Monoid[F], mt: Monoid[T]) = new Monoid[F :: T] {
      def zero = mh.zero :: mt.zero
      def append(a: F :: T, b: F :: T) = mh.append(a.head, b.head) :: mt.append(a.tail, b.tail)
    }

    def project[F, G](instance: ⇒ Monoid[G], to: F ⇒ G, from: G ⇒ F) = new Monoid[F] {
      def zero = from(instance.zero)
      def append(a: F, b: F) = from(instance.append(to(a), to(b)))
    }
  }
}

trait MonoidSyntax[T] {
  def |+|(b: T): T
}

object MonoidSyntax {
  implicit def monoidSyntax[T](a: T)(implicit mt: Monoid[T]): MonoidSyntax[T] = new MonoidSyntax[T] {
    def |+|(b: T) = mt.append(a, b)
  }
}

/** == Automatic type class instance derivation ==
  *
  * Based on and extending `Generic` and `LabelledGeneric`, Lars Hupel ([[https://twitter.com/larsr_h @larsr_h]]) has contributed the `TypeClass`
  * family of type classes, which provide automatic type class derivation facilities roughly equivalent to those available
  * with GHC as described in [[http://dreixel.net/research/pdf/gdmh.pdf "A Generic Deriving Mechanism for Haskell"]].  There is a description of an
  * earlier iteration of the Scala mechanism [[http://typelevel.org/blog/2013/06/24/deriving-instances-1.html here]], and examples of its use deriving `Show` and `Monoid`
  * instances [[https://github.com/milessabin/shapeless/blob/master/examples/src/main/scala/shapeless/examples/shows.scala here]]
  * and [[https://github.com/milessabin/shapeless/blob/master/examples/src/main/scala/shapeless/examples/monoids.scala here]] for labelled coproducts and unlabelled products respectively.
  *
  * For example, in the `Monoid` case, once the general deriving infrastructure for monoids is in place, instances are
  * automatically available for arbitrary case classes without any additional boilerplate
  *
  * {{{
  * trait Monoid[T] {
  * def zero: T
  * def append(a: T, b: T): T
  * }
  *
  * object Monoid extends ProductTypeClassCompanion[Monoid] {
  * def mzero[T](implicit mt: Monoid[T]) = mt.zero
  *
  * implicit def booleanMonoid: Monoid[Boolean] = new Monoid[Boolean] {
  * def zero = false
  * def append(a: Boolean, b: Boolean) = a || b
  * }
  *
  * implicit def intMonoid: Monoid[Int] = new Monoid[Int] {
  * def zero = 0
  * def append(a: Int, b: Int) = a+b
  * }
  *
  * implicit def doubleMonoid: Monoid[Double] = new Monoid[Double] {
  * def zero = 0.0
  * def append(a: Double, b: Double) = a+b
  * }
  *
  * implicit def stringMonoid: Monoid[String] = new Monoid[String] {
  * def zero = ""
  * def append(a: String, b: String) = a+b
  * }
  *
  * object typeClass extends ProductTypeClass[Monoid] {
  * def emptyProduct = new Monoid[HNil] {
  * def zero = HNil
  * def append(a: HNil, b: HNil) = HNil
  * }
  *
  * def product[F, T <: HList](mh: Monoid[F], mt: Monoid[T]) = new Monoid[F :: T] {
  * def zero = mh.zero :: mt.zero
  * def append(a: F :: T, b: F :: T) = mh.append(a.head, b.head) :: mt.append(a.tail, b.tail)
  * }
  *
  * def project[F, G](instance: => Monoid[G], to: F => G, from: G => F) = new Monoid[F] {
  * def zero = from(instance.zero)
  * def append(a: F, b: F) = from(instance.append(to(a), to(b)))
  * }
  * }
  * }
  *
  * trait MonoidSyntax[T] {
  * def |+|(b: T): T
  * }
  *
  * object MonoidSyntax {
  * implicit def monoidSyntax[T](a: T)(implicit mt: Monoid[T]): MonoidSyntax[T] = new MonoidSyntax[T] {
  * def |+|(b: T) = mt.append(a, b)
  * }
  * }
  * }}}
  * The [[https://github.com/typelevel/shapeless-contrib shapeless-contrib]] project also contains automatically derived type class instances for
  * [[https://github.com/typelevel/shapeless-contrib/blob/master/scalaz/main/scala/typeclass.scala Scalaz]],
  * [[https://github.com/typelevel/shapeless-contrib/blob/master/spire/main/scala/typeclass.scala Spire]] and
  * [[https://github.com/typelevel/shapeless-contrib/blob/master/scalacheck/main/scala/package.scala Scalacheck]].
  *
  * @param name auto_typeclass_derivation
  *
  */
object AutoTypeClassExercises extends FlatSpec with Matchers with exercise.Section {

  object Helper {

    // A pair of arbitrary case classes
    case class Foo(i: Int, s: String)
    case class Bar(b: Boolean, s: String, d: Double)
  }

  import Helper._

  /** {{{
    *
    * // A pair of arbitrary case classes
    * case class Foo(i : Int, s : String)
    * case class Bar(b : Boolean, s : String, d : Double)
    *
    * }}}
    */
  def monoidDerivation(res0: Int, res1: String, res2: Boolean, res3: String, res4: Double) = {

    import MonoidSyntax._
    import Monoid.typeClass._

    val fooCombined = Foo(13, "foo") |+| Foo(23, "bar")
    fooCombined should be(Foo(res0, res1))

    val barCombined = Bar(true, "foo", 1.0) |+| Bar(false, "bar", 3.0)
    barCombined should be(Bar(res2, res3, res4))

  }

}
