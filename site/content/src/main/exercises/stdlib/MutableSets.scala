package stdlib

import org.scalatest._

import scala.collection.mutable

/** mutable_sets
  *
  */
object MutableSets extends FlatSpec with Matchers with exercise.Section {


  /** Mutable sets can be created easily:
    */
  def easilyCreatedMutableSets(res0: Int, res1: Boolean) {
    val mySet = mutable.Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    mySet.size should be(res0)
    mySet += "Oregon"
    mySet contains "Oregon" should be(res1)
  }

  /** Mutable sets can have elements removed:
    */
  def removeElementMutableSets(res0: Boolean) {
    val mySet = mutable.Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    mySet -= "Ohio"
    mySet contains "Ohio" should be(res0)
  }

  /** Mutable sets can have tuples of elements removed:
    */
  def removeWithTuplesMutableSets(res0: Boolean, res1: Int) {
    val mySet = mutable.Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    mySet -=("Iowa", "Ohio")
    mySet contains "Ohio" should be(res0)
    mySet.size should be(res1)
  }

  /** Mutable sets can have tuples of elements added
    */
  def addWithTuplesMutableSets(res0: Boolean, res1: Int) {
    val mySet = mutable.Set("Michigan", "Wisconsin")
    mySet +=("Iowa", "Ohio")
    mySet contains "Ohio" should be(res0)
    mySet.size should be(res1)
  }

  /** Mutable sets can have Lists of elements added:
    */
  def addedElementsMutableSets(res0: Boolean, res1: Int) {
    val mySet = mutable.Set("Michigan", "Wisconsin")
    mySet ++= List("Iowa", "Ohio")
    mySet contains "Ohio" should be(res0)
    mySet.size should be(res1)
  }

  /** Mutable sets can have Lists of elements removed:
    */
  def removedElementsMutableSets(res0: Boolean, res1: Int) {
    val mySet = mutable.Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    mySet --= List("Iowa", "Ohio")
    mySet contains "Ohio" should be(res0)
    mySet.size should be(res1)
  }

  /** Mutable sets can be cleared:
    */
  def clearSetMutableSets(res0: Boolean, res1: Int) {
    val mySet = mutable.Set("Michigan", "Ohio", "Wisconsin", "Iowa")
    mySet.clear() // Convention is to use parens if possible when method called changes state
    mySet contains "Ohio" should be(res0)
    mySet.size should be(res1)
  }

}
