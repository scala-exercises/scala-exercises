package stdlib

import OptionsHelper._
import org.scalatest._

/** @param name options
  *
  */
object Options extends FlatSpec with Matchers with exercise.Section {

  /** If you have worked with Java at all in the past, it is very likely that you have come across a `NullPointerException` at some time (other languages will throw similarly named errors in such a case). Usually this happens because some method returns null when you were not expecting it and thus not dealing with that possibility in your client code. A value of `null` is often abused to represent an absent optional value.
    *
    * Scala tries to solve the problem by getting rid of `null` values altogether and providing its own type for representing optional values, i.e. values that may be present or not: the `Option[A]` trait.
    *
    * `Option[A]` is a container for an optional value of type `A`. If the value of type `A` is present, the `Option[A]` is an instance of `Some[A]`, containing the present value of type `A.` If the value is absent, the `Option[A]` is the object `None`.
    */
  def conceptOptions(res0: Option[String], res1: Option[String]) {
    val someValue: Option[String] = Some("I am wrapped in something")
    someValue should be(res0)

    val emptyValue: Option[String] = None
    emptyValue should be(res1)
  }

  /** Using `getOrElse` we can provide a default value ("No value") when the optional argument (`None`) does not exist:
    */
  def getOrElseOptions(res0: String, res1: String, res2: String) {
    val value1 = maybeItWillReturnSomething(true)
    val value2 = maybeItWillReturnSomething(false)

    value1 getOrElse "No value" should be(res0)
    value2 getOrElse "No value" should be(res1)
    value2 getOrElse {
      "default function"
    } should be(res2)

  }

  /** Checking whether option has value:
    */
  def isEmptyOptions(res0: Boolean, res1: Boolean) {
    val value1 = maybeItWillReturnSomething(true)
    val value2 = maybeItWillReturnSomething(false)

    value1.isEmpty should be(res0)
    value2.isEmpty should be(res1)
  }

  /** Option can also be used with pattern matching:
    */
  def matchOptions(res0: Float, res1: Float) {
    val someValue: Option[Double] = Some(20.0)
    val value = someValue match {
      case Some(v) ⇒ v
      case None    ⇒ 0.0
    }
    value should be(res0)
    val noValue: Option[Double] = None
    val value1 = noValue match {
      case Some(v) ⇒ v
      case None    ⇒ 0.0
    }
    value1 should be(res1)
  }

  /** An alternative for pattern matching is performing collection style operations.
    * This is possible because an option could be looked at as a collection with either one or zero elements.
    *
    * One of these operations is `map`. this operation allows to map the inner value to a different type while preserving the option
    */
  def mapOptions(res0: Option[Double], res1: Option[Double]) {
    val number: Option[Int] = Some(3)
    val noNumber: Option[Int] = None
    val result1 = number.map(_ * 1.5)
    val result2 = noNumber.map(_ * 1.5)

    result1 should be(res0)
    result2 should be(res1)

    /** Note that the type of result1 is now Option[Double], thanks to the scala type inference. */
  }

  /** Another operation is `fold`. this operation will extract the value from the option, or provide a default if the value is `None`
    */
  def foldOptions(res0: Int, res1: Int) {
    val number: Option[Int] = Some(3)
    val noNumber: Option[Int] = None
    val result1 = number.fold(0)(_ * 3)
    val result2 = noNumber.fold(0)(_ * 3)

    result1 should be(res0)
    result2 should be(res1)
  }

}
