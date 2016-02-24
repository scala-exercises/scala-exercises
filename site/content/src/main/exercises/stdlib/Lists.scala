package stdlib

import org.scalatest._

/** lists
  *
  */
object Lists extends FlatSpec with Matchers with exercise.Section {


  /** Scala Lists are quite similar to arrays which means, all the elements of a list have the same type but there are two important differences. First, lists are immutable, which means elements of a list cannot be changed by assignment. Second, lists represent a linked list whereas arrays are flat. The type of a list that has elements of type `T` is written as `List[T]`.
    *
    * `eq` tests identity (same object)
    */
  def similarToArraysLists(res0: Boolean) {
    val a = List(1, 2, 3)
    val b = List(1, 2, 3)
    (a eq b) should be(res0)
  }

  /** `==` tests equality (same content)
    */
  def sameContentLists(res0: Boolean) {
    val a = List(1, 2, 3)
    val b = List(1, 2, 3)
    (a == b) should be(res0)
  }

  /** Nil lists are identical, even of different types
    */
  def nilListsLists(res0: Boolean, res1: Boolean, res2: Boolean, res3: Boolean, res4: Boolean, res5: Boolean) {
    val a: List[String] = Nil
    val b: List[Int] = Nil

    (a == Nil) should be(res0)
    (a eq Nil) should be(res1)

    (b == Nil) should be(res2)
    (b eq Nil) should be(res3)

    (a == b) should be(res4)
    (a eq b) should be(res5)
  }

  /** Lists are easily created
    */
  def easilyCreatedLists(res0: Int, res1: Int, res2: Int) {
    val a = List(1, 2, 3)
    a should equal(List(res0, res1, res2))
  }

  /** Lists can be accessed via head and tail
    */
  def headAndTailLists(res0: Int, res1: Int, res2: Int) {
    val a = List(1, 2, 3)
    a.head should equal(res0)
    a.tail should equal(List(res1, res2))
  }

  /** Lists can be accessed by position
    */
  def byPositionLists(res0: Int, res1: Int, res2: Int) {
    val a = List(1, 3, 5, 7, 9)
    a(0) should equal(res0)
    a(2) should equal(res1)
    a(4) should equal(res2)

    intercept[IndexOutOfBoundsException] {
      println(a(5))
    }
  }

  /** Lists are immutable
    */
  def areInmutableLists(res0: Int, res1: Int, res2: Int, res3: Int) {
    val a = List(1, 3, 5, 7, 9)
    val b = a.filterNot(v => v == 5) // remove where value is 5

    a should equal(List(1, 3, 5, 7, 9))
    b should equal(List(res0, res1, res2, res3))
  }

  /** Lists have many useful methods
    */
  def usefulMethodsLists(res0: Int, res1: Int, res2: Int, res3: Int, res4: Int, res5: Int, res6: Int, res7: Int, res8: Int, res9: Int, res10: Int, res11: Int, res12: Int, res13: Int, res14: Int, res15: Int, res16: Int, res17: Int) {
    val a = List(1, 3, 5, 7, 9)

    // get the length of the list
    a.length should equal(res0)

    // reverse the list
    a.reverse should equal(List(res1, res2, res3, res4, res5))

    // convert the list to a string representation
    a.toString should equal("List(res6, res7, res8, res9, res10)")

    // map a function to double the numbers over the list
    a.map { v => v * 2 } should equal(List(res11, res12, res13, res14, res15))

    // filter any values divisible by 3 in the list
    a.filter { v => v % 3 == 0 } should equal(List(res16, res17))
  }

  /** Functions over lists can use _ as shorthand
    */
  def wildcardAsShorhandLists(res0: Int, res1: Int, res2: Int, res3: Int) {
    val a = List(1, 2, 3)

    a.map {
      _ * 2
    } should equal(List(res0, res1, res2))

    a.filter {
      _ % 2 == 0
    } should equal(List(res3))
  }

  /** Functions over lists can use `()` instead of `{}`
    */
  def functionsOverListsLists(res0: Int, res1: Int, res2: Int, res3: Int, res4: Int) {
    val a = List(1, 2, 3)
    a.map(_ * 2) should equal(List(res0, res1, res2))
    a.filter(_ % 2 != 0) should equal(List(res3, res4))
  }

  /** Lists can be *reduced* with a mathematical operation
    */
  def reducingListsLists(res0: Int, res1: Int) {
    val a = List(1, 3, 5, 7)
    a.reduceLeft(_ + _) should equal(res0)
    a.reduceLeft(_ * _) should equal(res1)
  }

  /** Foldleft is like reduce, but with an explicit starting value
    */
  def foldLeftLists(res0: Int, res1: Int, res2: Int, res3: Int) {
    val a = List(1, 3, 5, 7)
    // NOTE: foldLeft uses a form called currying that we will explore later
    a.foldLeft(0)(_ + _) should equal(res0)
    a.foldLeft(10)(_ + _) should equal(res1)
    a.foldLeft(1)(_ * _) should equal(res2)
    a.foldLeft(0)(_ * _) should equal(res3)
  }

  /** You can create a list from a range
    */
  def fromRangeLists(res0: Int, res1: Int, res2: Int, res3: Int, res4: Int) {
    val a = (1 to 5).toList
    a should be(List(res0, res1, res2, res3, res4))
  }

  /** Lists reuse their tails
    */
  def reuseTailsLists(res0: Int, res1: Int, res2: Int, res3: String, res4: String, res5: String) {
    val d = Nil
    val c = 3 :: d
    val b = 2 :: c
    val a = 1 :: b

    a should be(List(res0, res1, res2))
    a.tail should be(res3)
    b.tail should be(res4)
    c.tail should be(res5)
  }

}
