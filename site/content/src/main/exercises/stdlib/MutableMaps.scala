package stdlib

import org.scalatest._

import scala.collection.mutable

/** mutable_maps
  *
  */
object MutableMaps extends FlatSpec with Matchers with exercise.Section {


  /** easilyCreatedMutableMaps
    *
    * Mutable maps can be created easily:
    */
  def easilyCreatedMutableMaps(res0: Int, res1: Boolean) {
    val myMap = mutable.Map("MI" -> "Michigan", "OH" -> "Ohio", "WI" -> "Wisconsin", "IA" -> "Iowa")
    myMap.size should be(res0)
    myMap += "OR" -> "Oregon"
    myMap contains "OR" should be(res1)
  }

  /** removeElementMutableMaps
    *
    * Mutable maps can have elements removed:
    */
  def removeElementMutableMaps(res0: Boolean) {
    val myMap = mutable.Map("MI" -> "Michigan", "OH" -> "Ohio", "WI" -> "Wisconsin", "IA" -> "Iowa")
    myMap -= "OH"
    myMap contains "OH" should be(res0)
  }

  /** removeWithTuplesMutableMaps
    *
    * Mutable maps can have tuples of elements removed:
    */
  def removeWithTuplesMutableMaps(res0: Boolean, res1: Int) {
    val myMap = mutable.Map("MI" -> "Michigan", "OH" -> "Ohio", "WI" -> "Wisconsin", "IA" -> "Iowa")
    myMap -=("IA", "OH")
    myMap contains "OH" should be(res0)
    myMap.size should be(res1)
  }

  /** addWithTuplesMutableMaps
    *
    * Mutable maps can have tuples of elements added:
    */
  def addWithTuplesMutableMaps(res0: Boolean, res1: Int) {
    val myMap = mutable.Map("MI" -> "Michigan", "WI" -> "Wisconsin")
    myMap +=("IA" -> "Iowa", "OH" -> "Ohio")
    myMap contains "OH" should be(res0)
    myMap.size should be(res1)
  }

  /** addedElementsMutableMaps
    *
    * Mutable maps can have Lists of elements added:
    */
  def addedElementsMutableMaps(res0: Boolean, res1: Int) {
    val myMap = mutable.Map("MI" -> "Michigan", "WI" -> "Wisconsin")
    myMap ++= List("IA" -> "Iowa", "OH" -> "Ohio")
    myMap contains "OH" should be(res0)
    myMap.size should be(res1)
  }

  /** removedElementsMutableMaps
    *
    * Mutable maps can have Lists of elements removed:
    */
  def removedElementsMutableMaps(res0: Boolean, res1: Int) {
    val myMap = mutable.Map("MI" -> "Michigan", "OH" -> "Ohio", "WI" -> "Wisconsin", "IA" -> "Iowa")
    myMap --= List("IA", "OH")
    myMap contains "OH" should be(res0)
    myMap.size should be(res1)
  }

  /** clearMapMutableMaps
    *
    * Mutable maps can be cleared:
    */
  def clearMapMutableMaps(res0: Boolean, res1: Int) {
    val myMap = mutable.Map("MI" -> "Michigan", "OH" -> "Ohio", "WI" -> "Wisconsin", "IA" -> "Iowa")
    myMap.clear() // Convention is to use parens if possible when method called changes state
    myMap contains "OH" should be(res0)
    myMap.size should be(res1)
  }

}