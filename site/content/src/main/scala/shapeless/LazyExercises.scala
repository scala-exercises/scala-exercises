package shapelessex

import org.scalatest._
import shapeless._

/** == First class lazy values tie implicit recursive knots ==
  *
  * Traversals and transformations of recursive types (eg. cons lists or trees) must themselves be recursive. Consequently
  * type class instances which perform such operations must be recursive values in turn. This is problematic in Scala
  * at the both the value and the type levels: at the value level the issue is that recursive type class instances would
  * have to be constructed lazily, whilst Scala doesn't natively support lazy implicit arguments; at the type level the
  * issue is that during the type checking of expressions constructing recursive implicit values the implicit
  * resolution mechanism would revisit types in a way that would trip the divergence checker.
  *
  * The `Lazy[T]` type constructor and associated macro in shapeless addresses both of these problems in many cases. It is
  * similar to Scalaz's `Need[T]` and adds lazy implicit construction and suppression of divergence checking. This
  * supports constructions such as...
  *
  * @param name lazy
  */
object LazyExercises extends FlatSpec with Matchers with exercise.Section {

  object Helper {

    sealed trait List[+T]
    case class Cons[T](hd: T, tl: List[T]) extends List[T]
    sealed trait Nil extends List[Nothing]
    case object Nil extends Nil

    trait Show[T] {
      def apply(t: T): String
    }

    object Show {
      // Base case for Int
      implicit def showInt: Show[Int] = new Show[Int] {
        def apply(t: Int) = t.toString
      }

      // Base case for Nil
      implicit def showNil: Show[Nil] = new Show[Nil] {
        def apply(t: Nil) = "Nil"
      }

      // Case for Cons[T]: note (mutually) recursive implicit argument referencing Show[List[T]]
      implicit def showCons[T](implicit st: Lazy[Show[T]], sl: Lazy[Show[List[T]]]): Show[Cons[T]] = new Show[Cons[T]] {
        def apply(t: Cons[T]) = s"Cons(${show(t.hd)(st.value)}, ${show(t.tl)(sl.value)})"
      }

      // Case for List[T]: note (mutually) recursive implicit argument referencing Show[Cons[T]]
      implicit def showList[T](implicit sc: Lazy[Show[Cons[T]]]): Show[List[T]] = new Show[List[T]] {
        def apply(t: List[T]) = t match {
          case n: Nil     ⇒ show(n)
          case c: Cons[T] ⇒ show(c)(sc.value)
        }
      }
    }

    def show[T](t: T)(implicit s: Show[T]) = s(t)

    val l: List[Int] = Cons(1, Cons(2, Cons(3, Nil)))
  }

  import Helper._

  /** {{{
    * sealed trait List[+T]
    * case class Cons[T](hd: T, tl: List[T]) extends List[T]
    * sealed trait Nil extends List[Nothing]
    * case object Nil extends Nil
    *
    * trait Show[T] {
    * def apply(t: T): String
    * }
    *
    * object Show {
    * // Base case for Int
    * implicit def showInt: Show[Int] = new Show[Int] {
    * def apply(t: Int) = t.toString
    * }
    *
    * // Base case for Nil
    * implicit def showNil: Show[Nil] = new Show[Nil] {
    * def apply(t: Nil) = "Nil"
    * }
    *
    * // Case for Cons[T]: note (mutually) recursive implicit argument referencing Show[List[T]]
    * implicit def showCons[T](implicit st: Lazy[Show[T]], sl: Lazy[Show[List[T]]]): Show[Cons[T]] = new Show[Cons[T]] {
    * def apply(t: Cons[T]) = s"Cons(${show(t.hd)(st.value)}, ${show(t.tl)(sl.value)})"
    * }
    *
    * // Case for List[T]: note (mutually) recursive implicit argument referencing Show[Cons[T]]
    * implicit def showList[T](implicit sc: Lazy[Show[Cons[T]]]): Show[List[T]] = new Show[List[T]] {
    * def apply(t: List[T]) = t match {
    * case n: Nil => show(n)
    * case c: Cons[T] => show(c)(sc.value)
    * }
    * }
    * }
    *
    * def show[T](t: T)(implicit s: Show[T]) = s(t)
    *
    * val l: List[Int] = Cons(1, Cons(2, Cons(3, Nil)))
    * }}}
    */
  def lazyExercise(res0: String) = {
    show(l) should be(res0) // Without the Lazy wrappers above the following would diverge ...

    /** which would otherwise be impossible in Scala.
      */
  }

}
