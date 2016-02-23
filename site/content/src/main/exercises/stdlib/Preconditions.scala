package stdlib

import stdlib.PreconditionsHelper._
import org.scalatest._

/** preconditions
  *
  */
object Preconditions extends FlatSpec with Matchers with exercise.Section {


  /** preconditionsPreconditions
    *
    * One of the benefits of object-oriented programming is that it allows you to encapsulate data inside objects so that you can ensure the data is valid throughout its lifetime. In the case of an immutable object such as *Rational*, this means that you should ensure the data is valid when the object is constructed.
    *
    * Given that a zero denominator is an invalid state for a *Rational* number, you should not let a *Rational* be constructed if a zero is passed in the d parameter.
    *
    * The best way to approach this problem is to define as a precondition of the primary constructor that d must be non-zero. A precondition is a constraint on values passed into a method or constructor, a requirement which callers must fulfill.
    *
    * One way to do that is to use `require`, like this:
    *
    * {{{
    * class Rational(n: Int, d: Int) {
    *   require(d != 0)
    *   override def toString = n +"/"+ d
    * }
    * }}}
    *
    * The require method takes one boolean parameter. If the passed value is `true`, require will return normally. Otherwise, require will prevent the object from being constructed by throwing an `IllegalArgumentException`.
    *
    * Given:
    *
    * {{{
    * class WithParameterRequirement(val myValue: Int) {
    *   require(myValue != 0)
    *
    *   def this(someValue: String) {
    *     this(someValue.size)
    *   }
    * }
    * }}}
    * On precondition violation, intercept expects type of exception thrown. *Instruction: use Intercept to catch the type of exception thrown by an invalid precondition*
    */
  def preconditionsPreconditions(res0: Int) {
    val myInstance = new WithParameterRequirement("Do you really like my hair?")
    myInstance.myValue should be(res0)

    intercept[IllegalArgumentException] {
      new WithParameterRequirement("")
    }
  }

}