package stdlib

import org.scalatest._

/** sequences_and_arrays
  *
  */
object SequencesandArrays extends FlatSpec with Matchers with exercise.Section {


  /** Scala provides a data structure, the array, which stores a fixed-size sequential collection of elements of the same type. An array is used to store a collection of data, but it is often more useful to think of an array as a collection of variables of the same type.
    *
    * A list can be converted to an array:
    */
  def sequentialCollectionSequencesandArrays(res0: Int, res1: Int, res2: Int) {
    val l = List(1, 2, 3)
    val a = l.toArray
    a should equal(Array(res0, res1, res2))
  }

  /** Sequences are special cases of iterable collections of class `Iterable`. Unlike iterables, sequences always have a defined order of elements.
    *
    * Any sequence can be converted to a list:
    *
    */
  def orderedElementsSequencesandArrays(res0: String) {
    val a = Array(1, 2, 3)
    val s = a.toSeq
    val l = s.toList
    l should equal(res0)
  }

  /** You can create a sequence from a for comprehension:
    */
  def fromForComprehensionSequencesandArrays(res0: String) {
    val s = for (v <- 1 to 4) yield v
    s.toList should be(res0)
  }

  /** You can create a sequence from a for comprehension with a condition:
    */
  def withConditionSequencesandArrays(res0: String) {
    val s = for (v <- 1 to 10 if v % 3 == 0) yield v
    s.toList should be(res0)
  }

  /** You can filter any sequence based on a predicate:
    */
  def filterPredicateSequencesandArrays(res0: String, res1: String) {
    val s = Seq("hello", "to", "you")
    val filtered = s.filter(_.length > 2)
    filtered should be(Seq(res0, res1))
  }

  /** You can also filter Arrays in the same way:
    */
  def filterArraySequencesandArrays(res0: String, res1: String) {
    val a = Array("hello", "to", "you", "again")
    val filtered = a.filter(_.length > 3)
    filtered should be(Array(res0, res1))
  }

  /** You can map values in a sequence through a function:
    */
  def mapValuesSequencesandArrays(res0: String) {
    val s = Seq("hello", "world")
    val r = s map {
      _.reverse
    }

    r should be(res0)
  }

}
