package stdlib

import org.scalatest._

/** @param name val_and_var
  */
object ValandVar extends FlatSpec with Matchers with exercise.Section {

  /** Scala allows one to decide whether a variable is immutable or mutable. Immutable is read-only whereas mutable is read-write. Immutable variables are declared with the keyword `val`.
    *
    * {{{
    * val age:Int = 22
    * }}}
    *
    * Here age has been initialized to a value during its declaration. As it has been declared as a `val`, age cannot be reassigned to a new value. Any attempt to do so will result in a reassignment to `val` error.
    *
    * Mutable variables are declared with the keyword `var`. Unlike val, `var` can be reassigned to different values or point to different objects. But they have to be initialised at the time of declaration.
    *
    * {{{
    * var age:Int = 22
    * age = 35
    * }}}
    *
    * There's an exception to the rule that one must initialize the `val`'s and `var`'s. When they are used as constructor parameters, the `val`'s and `var`'s will be initialised when the object is instantiated. Also, derived classes can override `val`'s declared inside the parent classes.
    *
    * Your turn. Remember, `var`'s may be reassigned,
    */
  def mutableOrImmutableValandVar(res0: Int, res1: Int) {
    var a = 5
    a should be(res0)
    a = 7
    a should be(res1)
  }

  /** but `val`'s may not be reassigned.
    */
  def valIsInmutableValandVar(res0: Int) {
    val a = 5
    a should be(res0)

    // What happens if you uncomment these lines?
    // a = 7
    // a should be (7)
    /** Remember that `val` does not lock down the internal state of the variable, only its assignment.  Let us consider an Array being declared as `val`.
      *
      * {{{
      * val stringArray:Array[String] = new Array(6)
      * }}}
      * The `stringArray` can be modified, but the reference cannot be modified to point to another Array.  For example,
      *
      * {{{
      * stringArray = new Array(33)
      * }}}
      * will result in a reassignment to val error, but
      * {{{
      * stringArray(3) = "foo"
      * }}}
      * will not result in any error.
      */
  }

}
