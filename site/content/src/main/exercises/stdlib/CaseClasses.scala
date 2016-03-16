package stdlib

import org.scalatest._

/**
  * @param name case_classes
  */
object CaseClasses extends FlatSpec with Matchers with exercise.Section {


  /** Scala supports the notion of _case classes_. Case classes are regular classes which export their constructor parameters and which provide a recursive decomposition mechanism via pattern matching.
    *
    * Here is an example for a class hierarchy which consists of an abstract super class `Term` and three concrete case classes `Var`, `Fun`, and `App`.
    *
    * {{{
    * abstract class Term
    * case class Var(name: String) extends Term
    * case class Fun(arg: String, body: Term) extends Term
    * case class App(f: Term, v: Term) extends Term
    * }}}
    *
    * This class hierarchy can be used to represent terms of the untyped lambda calculus. To facilitate the construction of case class instances, Scala does not require that the `new` primitive is used. One can simply use the class name as a function.
    *
    * Here is an example:
    *
    * {{{
    * Fun("x", Fun("y", App(Var("x"), Var("y"))))
    * }}}
    *
    * The constructor parameters of case classes are treated as public values and can be accessed directly.
    *
    * {{{
    * val x = Var("x")
    * Console.println(x.name)
    * }}}
    *
    * For every case class the Scala compiler generates `equals` method which implements structural equality and a`toString` method. For instance:
    *
    * {{{
    * val x1 = Var("x")
    * val x2 = Var("x")
    * val y1 = Var("y")
    * println("" + x1 + " == " + x2 + " => " + (x1 == x2))
    * println("" + x1 + " == " + y1 + " => " + (x1 == y1))
    * }}}
    *
    * will print
    *
    * {{{
    * Var(x) == Var(x) => true
    * Var(x) == Var(y) => false
    * }}}
    *
    * It only makes sense to define case classes if pattern matching is used to decompose data structures. The following object defines a pretty printer function for our lambda calculus representation:
    *
    * {{{
    * object TermTest extends Application {
    *   def printTerm(term: Term) {
    *     term match {
    *       case Var(n) =>
    *         print(n)
    *       case Fun(x, b) =>
    *         print("^" + x + ".")
    *         printTerm(b)
    *       case App(f, v) =>
    *         Console.print("(")
    *         printTerm(f)
    *         print(" ")
    *         printTerm(v)
    *         print(")")
    *     }
    *   }
    *   def isIdentityFun(term: Term): Boolean = term match {
    *     case Fun(x, Var(y)) if x == y => true
    *     case _ => false
    *   }
    *   val id = Fun("x", Var("x"))
    *   val t = Fun("x", Fun("y", App(Var("x"), Var("y"))))
    *   printTerm(t)
    *   println
    *   println(isIdentityFun(id))
    *   println(isIdentityFun(t))
    * }
    * }}}
    *
    * In our example, the function `print` is expressed as a pattern matching statement starting with the `match` keyword and consisting of sequences of `case Pattern => Body` clauses.
    *
    * The program above also defines a function `isIdentityFun` which checks if a given term corresponds to a simple identity function. This example uses deep patterns and guards. After matching a pattern with a given value, the guard (defined after the keyword `if`) is evaluated. If it returns `true`, the match succeeds; otherwise, it fails and the next pattern will be tried.
    *
    * Case classes have an automatic equals method that works:
    */
  def caseClassesSupportEquality(res0: Boolean, res1: Boolean, res2: Boolean, res3: Boolean) {
    case class Person(first: String, last: String)

    val p1 = new Person("Fred", "Jones")
    val p2 = new Person("Shaggy", "Rogers")
    val p3 = new Person("Fred", "Jones")

    (p1 == p2) should be(res0)
    (p1 == p3) should be(res1)

    (p1 eq p2) should be(res2)
    (p1 eq p3) should be(res3) // not identical, merely equal
  }

  /** Case classes have an automatic hashcode method that works:
    */
  def hashcodeMethodCaseClasses(res0: Boolean, res1: Boolean) {
    case class Person(first: String, last: String)

    val p1 = new Person("Fred", "Jones")
    val p2 = new Person("Shaggy", "Rogers")
    val p3 = new Person("Fred", "Jones")

    (p1.hashCode == p2.hashCode) should be(res0)
    (p1.hashCode == p3.hashCode) should be(res1)
  }

  /** Case classes can be created in a convenient way:
    */
  def creationCaseClasses(res0: Boolean, res1: Boolean, res2: Boolean) {
    case class Dog(name: String, breed: String)

    val d1 = Dog("Scooby", "Doberman")
    val d2 = Dog("Rex", "Custom")
    val d3 = new Dog("Scooby", "Doberman") // the old way of creating using new

    (d1 == d3) should be(res0)
    (d1 == d2) should be(res1)
    (d2 == d3) should be(res2)
  }

  /** toStringMethodCaseClasses
    *
    * Case classes have a convenient toString method defined:
    */
  def toStringMethodCaseClasses(res0: String) {
    case class Dog(name: String, breed: String)
    val d1 = Dog("Scooby", "Doberman")
    d1.toString should be(res0)
  }

  /** Case classes have automatic properties:
    */
  def propertiesCaseClasses(res0: String, res1: String) {
    case class Dog(name: String, breed: String)

    val d1 = Dog("Scooby", "Doberman")
    d1.name should be(res0)
    d1.breed should be(res1)
  }

  /** Case classes can have mutable properties:
    */
  def mutablePropertiesCaseClasses(res0: String, res1: String, res2: String, res3: String) {
    case class Dog(var name: String, breed: String) // you can rename a dog, but change its breed? nah!
    val d1 = Dog("Scooby", "Doberman")

    d1.name should be(res0)
    d1.breed should be(res1)

    d1.name = "Scooby Doo" // but is it a good idea?

    d1.name should be(res2)
    d1.breed should be(res3)
  }

  /** There are safer alternatives for altering case classes:
    */
  def alteringCaseClasses(res0: String, res1: String, res2: String, res3: String) {
    case class Dog(name: String, breed: String) // Doberman

    val d1 = Dog("Scooby", "Doberman")

    val d2 = d1.copy(name = "Scooby Doo") // copy the case class but change the name in the copy

    d1.name should be(res0) // original left alone
    d1.breed should be(res1)

    d2.name should be(res2)
    d2.breed should be(res3) // copied from the original
  }

  /** Case classes can have default and named parameters:
    */
  def parametersCaseClasses(res0: String, res1: String, res2: Int, res3: String, res4: String, res5: String, res6: Int, res7: String, res8: String, res9: String, res10: Int, res11: String, res12: Boolean) {
    case class Person(first: String, last: String, age: Int = 0, ssn: String = "")
    val p1 = Person("Fred", "Jones", 23, "111-22-3333")
    val p2 = Person("Samantha", "Jones") // note missing age and ssn
    val p3 = Person(last = "Jones", first = "Fred", ssn = "111-22-3333") // note the order can change, and missing age
    val p4 = p3.copy(age = 23)

    p1.first should be(res0)
    p1.last should be(res1)
    p1.age should be(res2)
    p1.ssn should be(res3)

    p2.first should be(res4)
    p2.last should be(res5)
    p2.age should be(res6)
    p2.ssn should be(res7)

    p3.first should be(res8)
    p3.last should be(res9)
    p3.age should be(res10)
    p3.ssn should be(res11)

    (p1 == p4) should be(res12)
  }

  /** Case classes can be disassembled to their constituent parts as a tuple:
    */
  def asTupleCaseClasses(res0: String, res1: String, res2: Int, res3: String) {
    case class Person(first: String, last: String, age: Int = 0, ssn: String = "")
    val p1 = Person("Fred", "Jones", 23, "111-22-3333")

    val parts = Person.unapply(p1).get // this seems weird, but it's critical to other features of Scala

    parts._1 should be(res0)
    parts._2 should be(res1)
    parts._3 should be(res2)
    parts._4 should be(res3)
  }

  /** Case classes are Serializable
    */
  def serializableCaseClasses(res0: Boolean, res1: Boolean) {
    case class PersonCC(firstName: String, lastName: String)
    val indy = PersonCC("Indiana", "Jones")

    indy.isInstanceOf[Serializable] should be(res0)


    class Person(firstName: String, lastName: String)
    val junior = new Person("Indiana", "Jones")

    junior.isInstanceOf[Serializable] should be(res1)
  }

}
