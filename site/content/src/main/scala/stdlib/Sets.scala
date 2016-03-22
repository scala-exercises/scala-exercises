package stdlib

import org.scalatest._

/** @param name sets
  *
  */
object Sets extends FlatSpec with Matchers with exercise.Section {


  /** `Set`s are `Iterable`s that contain no duplicate elements. The operations on sets are summarized in the following table for general sets and in the table after that for mutable sets. They fall into the following categories:
    *
    * *   **Tests** `contains`, `apply`, `subsetOf`. The `contains` method asks whether a set contains a given element. The `apply` method for a set is the same as `contains`, so `set(elem)` is the same as `set contains elem`. That means sets can also be used as test functions that return true for the elements they contain.
    * *   **Additions** `+` and `++`, which add one or more elements to a set, yielding a new set.
    * *   **Removals** `-`, `--`, which remove one or more elements from a set, yielding a new set.
    * *   **Set operations** for union, intersection, and set difference. Each of these operations exists in two forms: alphabetic and symbolic. The alphabetic versions are `intersect`, `union`, and `diff`, whereas the symbolic versions are `&`, `|`, and `&~`. In fact, the `++` that Set inherits from `Traversable` can be seen as yet another alias of `union` or `|`, except that `++` takes a `Traversable` argument whereas `union` and `|` take sets.
    *
    * Sets can be created easily:
    */
  def noDuplicatesSets(res0: Int) {
    val mySet = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    mySet.size should be(res0)
  }

  /** Sets contain distinct values:
    */
  def distinctValuesSets(res0: Int) {
    val mySet = Set("Michigan", "Ohio", "Wisconsin", "Michigan")
    mySet.size should be(res0)
  }

  /** Sets can be added to easily:
    */
  def easilyAddedSets(res0: Boolean, res1: Boolean) {
    val mySet = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val aNewSet = mySet + "Illinois"

    aNewSet.contains("Illinois") should be(res0)
    mySet.contains("Illinois") should be(res1)
  }

  /** Sets may be of mixed type:
    */
  def mixedTypeSets(res0: Boolean, res1: Boolean) {
    val mySet = Set("Michigan", "Ohio", 12)

    mySet.contains(12) should be(res0)
    mySet.contains("MI") should be(res1)
  }

  /** Sets can be checked for member existence:
    */
  def checkExistenceSets(res0: Boolean, res1: Boolean) {
    val mySet = Set("Michigan", "Ohio", 12)

    mySet(12) should be(res0)
    mySet("MI") should be(res1)
  }

  /** Set elements can be removed easily:
    */
  def easilyRemovedSets(res0: Boolean, res1: Boolean) {
    val mySet = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val aNewSet = mySet - "Michigan"

    aNewSet.contains("Michigan") should be(res0)
    mySet.contains("Michigan") should be(res1)
  }

  /** Set elements can be removed in multiple:
    */
  def multipleRemovingSets(res0: Boolean, res1: Boolean, res2: Int) {
    val mySet = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val aNewSet = mySet -- List("Michigan", "Ohio")

    aNewSet.contains("Michigan") should be(res0)
    aNewSet.contains("Wisconsin") should be(res1)
    aNewSet.size should be(res2)
  }

  /** Set elements can be removed with a tuple:
    */
  def tupleRemovingSets(res0: Boolean, res1: Boolean, res2: Int) {
    val mySet = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val aNewSet = mySet -("Michigan", "Ohio") // Notice: single '-' operator for tuples

    aNewSet.contains("Michigan") should be(res0)
    aNewSet.contains("Wisconsin") should be(res1)
    aNewSet.size should be(res2)
  }

  /** Attempted removal of nonexistent elements from a set is handled gracefully:
    */
  def nonexistentRemovalSets(res0: Boolean) {
    val mySet = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val aNewSet = mySet - "Minnesota"

    aNewSet.equals(mySet) should be(res0)
  }

  /** Sets can be iterated easily:
    */
  def easilyIteratedSets(res0: Int) {
    val mySet = Set(1, 3, 4, 9)
    var sum = 0
    for (i <- mySet)
      sum = sum + i //Of course this is the same thing as mySet.reduce(_ + _) or mySet.sum

    sum should be(res0)
  }

  /** Two sets can be intersected easily:
    */
  def easilyIntersectedSets(res0: Boolean) {
    val mySet1 = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val mySet2 = Set("Wisconsin", "Michigan", "Minnesota")
    val aNewSet = mySet1 intersect mySet2
    // NOTE: Scala 2.7 used **, deprecated for & or intersect in Scala 2.8

    aNewSet.equals(Set("Michigan", "Wisconsin")) should be(res0)
  }

  /** Two sets can be joined as their union easily:
    */
  def easilyJoinedSets(res0: Boolean) {
    val mySet1 = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val mySet2 = Set("Wisconsin", "Michigan", "Minnesota")
    val aNewSet = mySet1 union mySet2 // NOTE: You can also use the "|" operator

    aNewSet.equals(Set("Michigan", "Wisconsin", "Ohio", "Iowa", "Minnesota")) should be(res0)
  }

  /** A set is either a subset of another set or it isn't:
    */
  def subsetSets(res0: Boolean, res1: Boolean) {
    val mySet1 = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val mySet2 = Set("Wisconsin", "Michigan", "Minnesota")
    val mySet3 = Set("Wisconsin", "Michigan")

    mySet2 subsetOf mySet1 should be(res0)
    mySet3 subsetOf mySet1 should be(res1)
  }

  /** The difference between two sets can be obtained easily:
    */
  def easilyObtainedDifferencesSets(res0: Boolean) {
    val mySet1 = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val mySet2 = Set("Wisconsin", "Michigan")
    val aNewSet = mySet1 diff mySet2 // Note: you can use the "&~" operator if you *really* want to.

    aNewSet.equals(Set("Ohio", "Iowa")) should be(res0)
  }

  /** Set equivalency is independent of order:
    */
  def equivalencySets(res0: Boolean) {
    val mySet1 = Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    val mySet2 = Set("Wisconsin", "Michigan", "Ohio", "Iowa")

    mySet1.equals(mySet2) should be(res0)
  }

}
