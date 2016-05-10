package stdlib

import org.scalatest._

/** @param name constructors
  *
  */
object Constructors extends FlatSpec with Matchers with exercise.Section {

  /** Primary Constructor
    *
    * In Java we have a no-args default constructor which is provided for every class which doesn't provide its own constructor methods. On a similar lines Primary Constructor in Scala is the kind-of default constructor in the way every class in Scala would have a Primary Constructor.
    *
    * The primary constructor spans the complete class definition i.e in the example below the age field gets initialized as part of the Primary Constructor of the Employee class.
    *
    * {{{
    * class Employee {
    * var age:Int = 20
    * }
    * }}}
    *
    *
    * In the above example the primary constructor didn't accept any parameters. But the primary constructor can also accepts parameters, this is where it is different from default constructors in Java. Let me not try to draw an analogy any further.
    *
    * {{{
    * class Employee(val firstName:String,
    * val lastName:String) {
    *
    * override def toString():String = {
    * "First Name: " + firstName + " Last Name: " + lastName
    * }
    * }
    * }}}
    *
    *
    * ==Auxiliary Constructor==
    *
    * In Java one can overload the constructors to provide different constructors accepting different parameters. On similar lines Scala classes can declare Auxiliary constructors which are overloaded forms of the Primary Constructor. The auxiliary constructors are named as **this**.
    * Lets see an example:
    *
    * {{{
    * class Employee(val firstName:String, val lastName:String) {
    * var age:Int = 0
    *
    * //Auxiliary Constructor
    * def this(firstName:String, lastName: String, age:Int) {
    * this(firstName,lastName)
    * this.age = age
    * }
    *
    * override def toString():String = {
    * "First Name: " + firstName + " Last Name: " + lastName
    * }
    * }
    * }}}
    *
    *
    * There's a catch here- The auxiliary constructor can invoke the primary constructor or an auxiliary constructor declared just before it, which means the below code will not work:
    *
    * {{{
    * class Employee(val firstName:String, val lastName:String) {
    * var age:Int = 0
    * var city:String = _
    *
    * def this(firstName:String, lastName: String,
    * city:String, age:Int) {
    * this(firstName, lastName, city)
    * this.age = age
    * }
    *
    * def this(firstName:String, lastName: String, city:String) {
    * this(firstName,lastName)
    * this.city = city
    * }
    *
    * override def toString():String = {
    * "First Name: " + firstName + " Last Name: " + lastName
    * }
    * }
    * }}}
    *
    * Trying to compile the above code results in:
    *
    * {{{
    * $ scalac Employee.scala
    * Employee.scala:9:error: called constructor's definition must
    * precede calling constructor's definition
    * this(firstName, lastName, city)
    * ^
    * one error found
    * }}}
    *
    *
    * The error clearly says what's wrong. One would have to take care of this, though the compiler would report such issues, but its always good to know.
    *
    * Your turn:
    *
    */
  def primaryConstructorConstructors(res0: String) {
    class AboutConstructorWithAuxiliaryConstructor(val name: String) {
      // invoke auxiliary constructor
      def this() {
        // what happens if you comment out the following line?
        this("defaultname")
      }
    }

    val aboutMe = new AboutConstructorWithAuxiliaryConstructor()
    aboutMe.name should be(res0)
  }

}
