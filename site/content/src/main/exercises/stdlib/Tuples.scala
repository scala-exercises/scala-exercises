package stdlib

import java.util.Date

import org.scalatest._

/** Tuples
  *
  * Tuples Description
  */
object Tuples extends FlatSpec with Matchers with exercise.Section {


  /** oneIndexedTuples
    *
    * Scala tuple combines a fixed number of items together so that they can be passed around as a whole. They are one indexed. Unlike an array or list, a tuple can hold objects with different types but they are also immutable. Here is an example of a tuple holding an integer, a string, and the console:
    *
    * {{{
    * val t = (1, "hello", Console)
    * }}}
    *
    * Which is syntactic sugar (short cut) for the following:
    *
    * {{{
    * val t = new Tuple3(1, "hello", Console)
    * }}}
    * As you can see, tuples can be created easily:
    */
  def oneIndexedTuples(res0: String, res1: String) {
    val tuple = ("apple", "dog")
    val fruit = tuple._1
    val animal = tuple._2

    fruit should be(res0)
    animal should be(res1)
  }

  /** mixedTypeTuples
    *
    * Tuples may be of mixed type:
    */
  def mixedTypeTuples(res0: String, res1: String) {
    val tuple5 = ("a", 1, 2.2, new Date(), BigDecimal(5))

    tuple5._2 should be(res0)
    tuple5._5 should be(res1)
  }

  /** assignVariablesTuples
    *
    * You can assign multiple variables at once using tuples:
    */
  def assignVariablesTuples(res0: String, res1: String, res2: String) {
    val student = ("Sean Rogers", 21, 3.5)
    val (name, age, gpa) = student

    name should be(res0)
    age should be(res1)
    gpa should be(res2)
  }

  /** swappedTuples
    *
    * Tuples items can be swapped on a Tuple 2
    */
  def swappedTuples(res0: String, res1: String) {
    val tuple = ("apple", 3).swap
    tuple._1 should be(res0)
    tuple._2 should be(res1)
  }

}