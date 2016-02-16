package stdlib

import org.scalatest._

/** Empty Values
  *
  * Empty Values Description
  */
object EmptyValues extends FlatSpec with Matchers with exercise.Section {


  /** emptyValuesEmptyValues
    *
    * ==null==
    *
    * Scala's `null` is the same as in Java. Any reference type can be `null`, like Strings, Objects, or your own classes. Also just like Java, value types like Ints can't be `null`. 
    *
    * ==Null==
    *
    * [Null](http://www.scala-lang.org/api/current/index.html#scala.Null) is a trait whose only instance is `null`. It is a subtype of all reference types, but not of value types. It purpose in existing is to make it so reference types can be assigned `null` and value types can't. 
    *
    * ==Nothing==
    *
    * [Nothing](http://www.scala-lang.org/api/current/index.html#scala.Nothing) is a trait that is guaranteed to have _zero_ instances. It is a subtype of all other types. It has two main reasons for existing: to provide a return type for methods that **never** return normally (i.e. a method that always throws an exception). The other reason is to provide a type for Nil (explained below). 
    *
    * ==Unit==
    *
    * [Unit](http://www.scala-lang.org/api/current/index.html#scala.Unit) in Scala is the equivalent of `void` in Java. It's used in a function's signature when that function doesn't return a value.
    *
    * ==Nil==
    *
    * [Nil](http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.Nil$) is just an empty list, exactly like the result of `List()`. It is of type `List[Nothing]`. And since we know there are no instances of Nothing, we now have a list that is statically verifiable as empty. Nice to have. 
    *
    * An empty list can be represented by another nothing value: *Nil*
    */
  def emptyValuesEmptyValues(res0: String) {
    assert(List() === res0)
  }

  /** avoidingNullEmptyValues
    *
    * [[http://www.scala-lang.org/api/current/index.html#scala.None None]] is the counterpart to [[http://www.scala-lang.org/api/current/index.html#scala.Some Some]], used when you're using Scala's [[http://www.scala-lang.org/api/current/index.html#scala.Option Option]] class to help avoid `null` references.
    *
    * *None* equals *None*:
    */
  def avoidingNullEmptyValues(res0: String) {
    assert(None === res0)
  }

  /** identicalNoneEmptyValues
    *
    * *None* should be identical to *None*:
    */
  def identicalNoneEmptyValues(res0: String) {
    val a = None
    val b = None
    assert(a eq res0)
  }

  /** noneToStringEmptyValues
    *
    * *None* can be converted to a *String*:
    */
  def noneToStringEmptyValues(res0: String) {
    assert(None.toString === res0)
  }

  /** noneToListEmptyValues
    *
    * *None* can be converted to an empty list
    */
  def noneToListEmptyValues(res0: String) {
    val a = None
    assert(a.toList === res0)
  }

  /** noneAsEmptyEmptyValues
    *
    * *None* is considered empty
    */
  def noneAsEmptyEmptyValues(res0: String) {
    assert(None.isEmpty === res0)
  }

  /** noneToAnyEmptyValues
    *
    * *None* can be cast *Any*, *AnyRef* or *AnyVal*
    */
  def noneToAnyEmptyValues(res0: String, res1: String, res2: String) {
    assert(None.asInstanceOf[Any] === res0)
    assert(None.asInstanceOf[AnyRef] === res1)
    assert(None.asInstanceOf[AnyVal] === res2)
  }

  /** noneWithOptionEmptyValues
    *
    * *None* can be used with *Option* instead of null references
    */
  def noneWithOptionEmptyValues(res0: String, res1: String) {
    val optional: Option[String] = None
    assert(optional.isEmpty === res0)
    assert(optional === res1)
  }

  /** someAgainstNoneEmptyValues
    *
    * *Some* is the opposite of *None* for *Option* types
    */
  def someAgainstNoneEmptyValues(res0: String, res1: String) {
    val optional: Option[String] = Some("Some Value")
    assert((optional == None) === res0, "Some(value) should not equal None")
    assert(optional.isEmpty === res1, "Some(value) should not be empty")
  }

  /** getOrElseEmptyValues
    *
    * `Option.getOrElse` can be used to provide a default in the case of *None*:
    */
  def getOrElseEmptyValues(res0: String, res1: String) {
    val optional: Option[String] = Some("Some Value")
    val optional2: Option[String] = None
    assert(optional.getOrElse("No Value") === res0, "Should return the value in the option")
    assert(optional2.getOrElse("No Value") === res1, "Should return the specified default value")
  }

}