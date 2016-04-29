package stdlib

import org.scalatest._

/** @param name maps
  *
  */
object Maps extends FlatSpec with Matchers with exercise.Section {

  /** A `Map` is an `Iterable` consisting of pairs of keys and values (also named mappings or associations). Scala's Predef class offers an implicit conversion that lets you write `key -> value` as an alternate syntax for the pair `(key, value)`. For instance `Map("x" -> 24, "y" -> 25, "z" -> 26)` means exactly the same as `Map(("x", 24), ("y", 25), ("z", 26))`, but reads better.
    *
    * The fundamental operations on maps are similar to those on sets. They are summarized in the following table and fall into the following categories:
    *
    * - Lookup operations `apply`, `get`, `getOrElse`, `contains`, and `isDefinedAt`. These turn maps into partial functions from keys to values. The fundamental lookup method for a map is: `def get(key): Option[Value]`. The operation "`m get key`" tests whether the map contains an association for the given key. If so, it returns the associated value in a `Some`. If no key is defined in the map, get returns `None`. Maps also define an `apply` method that returns the value associated with a given key directly, without wrapping it in an `Option`. If the key is not defined in the map, an exception is raised.
    * - Additions and updates `+`, `++`, `updated`, which let you add new bindings to a map or change existing bindings.
    * - Removals `-`, `--`, which remove bindings from a map.
    * - Subcollection producers `keys`, `keySet`, `keysIterator`, `values`, `valuesIterator`, which return a map's keys and values separately in various forms.
    * - Transformations `filterKeys` and `mapValues`, which produce a new map by filtering and transforming bindings of an existing map.
    *
    * Maps can be created easily:
    */
  def keyAndValueMaps(res0: Int) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "IA" → "Iowa")
    myMap.size should be(res0)
  }

  /** Maps contain distinct pairings:
    */
  def distinctPairingsMaps(res0: Int) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "MI" → "Michigan")
    myMap.size should be(res0)
  }

  /** Maps can be added to easily:
    */
  def easilyAddedMaps(res0: Boolean) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "MI" → "Michigan")
    val aNewMap = myMap + ("IL" → "Illinois")
    aNewMap.contains("IL") should be(res0)
  }

  /** Map values can be iterated:
    */
  def canBeIteratedMaps(res0: Int, res1: String, res2: String) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "MI" → "Michigan")

    val mapValues = myMap.values
    mapValues.size should be(res0)
    mapValues.head should be(res1) //Failed presumption: The order in maps is not guaranteed

    val lastElement = mapValues.last
    lastElement should be(res2) //Failed presumption: The order in maps is not guaranteed
  }

  /** Maps insertion with duplicate key updates previous entry with subsequent value:
    */
  def duplicatedKeyInsertionMaps(res0: Int, res1: String) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "MI" → "Meechigan")
    val mapValues = myMap.values
    mapValues.size should be(res0)
    myMap("MI") should be(res1)

  }

  /** Map keys may be of mixed type:
    */
  def mixedTypeKeysMaps(res0: String, res1: String) {
    val myMap = Map("Ann Arbor" → "MI", 49931 → "MI")
    myMap("Ann Arbor") should be(res0)
    myMap(49931) should be(res1)
  }

  /** Mixed type values can be added to a map:
    */
  def mixedTypeValuesMaps(res0: Int, res1: Int, res2: Int, res3: Int) {
    val myMap = scala.collection.mutable.Map.empty[String, Any]
    myMap("Ann Arbor") = (48103, 48104, 48108)
    myMap("Houghton") = 49931

    myMap("Houghton") should be(res0)
    myMap("Ann Arbor") should be((res1, res2, res3))
  }

  /** Maps may be accessed:
    */
  def mayBeAccessedMaps(res0: String, res1: String) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "IA" → "Iowa")
    myMap("MI") should be(res0)
    myMap("IA") should be(res1)
  }

  /** Map elements can be removed easily:
    */
  def easilyRemovedMaps(res0: Boolean, res1: Boolean) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "IA" → "Iowa")
    val aNewMap = myMap - "MI"
    aNewMap.contains("MI") should be(res0)
    myMap.contains("MI") should be(res1)
  }

  /** Accessing a map by key results in an exception if key is not found:
    */
  def keyNotFoundMaps(res0: Boolean) {
    val myMap = Map("OH" → "Ohio", "WI" → "Wisconsin", "IA" → "Iowa")
    var blewWithException = true
    intercept[NoSuchElementException] {
      myMap("MI")
      blewWithException = false
    }
    blewWithException should be(res0)
  }

  /** Map elements can be removed in multiple:
    */
  def removedInMultipleMaps(res0: Boolean, res1: Boolean, res2: Boolean, res3: Int, res4: Int) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "IA" → "Iowa")
    val aNewMap = myMap -- List("MI", "OH")

    aNewMap.contains("MI") should be(res0)
    myMap.contains("MI") should be(res1)

    aNewMap.contains("WI") should be(res2)
    aNewMap.size should be(res3)
    myMap.size should be(res4)
  }

  /** Map elements can be removed with a tuple:
    */
  def removedWithTupleMaps(res0: Boolean, res1: Boolean, res2: Boolean, res3: Int, res4: Int) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "IA" → "Iowa")
    val aNewMap = myMap - ("MI", "WI") // Notice: single '-' operator for tuples

    aNewMap.contains("MI") should be(res0)
    myMap.contains("MI") should be(res1)
    aNewMap.contains("OH") should be(res2)
    aNewMap.size should be(res3)
    myMap.size should be(res4)
  }

  /** Attempted removal of nonexistent elements from a map is handled gracefully:
    */
  def attemptedRemovalMaps(res0: Boolean) {
    val myMap = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "IA" → "Iowa")
    val aNewMap = myMap - "MN"

    aNewMap.equals(myMap) should be(res0)
  }

  /** Map equivalency is independent of order:
    */
  def orderIndependentMaps(res0: Boolean) {
    val myMap1 = Map("MI" → "Michigan", "OH" → "Ohio", "WI" → "Wisconsin", "IA" → "Iowa")
    val myMap2 = Map("WI" → "Wisconsin", "MI" → "Michigan", "IA" → "Iowa", "OH" → "Ohio")

    myMap1.equals(myMap2) should be(res0)
  }

}
