package stdlib

import ObjectsHelper._
import org.scalatest._

/** @param name objects
  *
  */
object Objects extends FlatSpec with Matchers with exercise.Section {

  /** An object is a singleton. One object, that's it. This object is a replacement of static in Java, and is called upon much in the same way.
    */
  def singletonObjects(res0: String, res1: String, res2: String, res3: String) {
    object Greeting {
      def english = "Hi"

      def espanol = "Hola"

      def deutsch = "Hallo"

      def magyar = "Szia"
    }

    Greeting.english should be(res0)
    Greeting.espanol should be(res1)
    Greeting.deutsch should be(res2)
    Greeting.magyar should be(res3)
  }

  /** Here is a proof that an object is a singleton, and not a static method in a class
    */
  def notStaticMethodObjects(res0: Boolean, res1: Boolean) {
    object Greeting {
      def english = "Hi"

      def espanol = "Hola"

      def deutsch = "Hallo"

      def magyar = "Szia"
    }

    val x = Greeting
    val y = x

    x eq y should be(res0) //Reminder: eq checks for reference

    val z = Greeting

    x eq z should be(res1)
  }

  /** An object that has the same name as a class is called a companion object of the class, and it is often used to contain factory methods for the class that it complements.
    */
  def companionObjectObjects(res0: String) {
    class Movie(val name: String, val year: Short)

    object Movie {
      def academyAwardBestMoviesForYear(x: Short) = {
        //This is a match statement, more powerful than a Java switch statement!
        x match {
          case 1930 ⇒ Some(new Movie("All Quiet On the Western Front", 1930))
          case 1931 ⇒ Some(new Movie("Cimarron", 1931))
          case 1932 ⇒ Some(new Movie("Grand Hotel", 1932))
          case _    ⇒ None
        }
      }
    }

    Movie.academyAwardBestMoviesForYear(1932).get.name should be(res0)
  }

  /** A companion object stores shared variables and values, for every instantiated class member to share. Having the next companion object `SecretAgent`:
    * {{{
    * class SecretAgent(val name: String) {
    * def shoot(n: Int) {
    *  SecretAgent.decrementBullets(n)
    * }
    * }
    *
    * object SecretAgent {
    * //This is encapsulated!
    * var bullets: Int = 3000
    *
    * private def decrementBullets(count: Int) {
    *  if (bullets - count <= 0) bullets = 0
    *  else bullets = bullets - count
    * }
    * }
    * }}}
    * Try to understand how bullets are decreasing while agents are shooting.
    */
  def sharingVariablesObjects(res0: Int) {
    val bond = new SecretAgent("James Bond")
    val felix = new SecretAgent("Felix Leitner")
    val jason = new SecretAgent("Jason Bourne")
    val _99 = new SecretAgent("99")
    val max = new SecretAgent("Max Smart")

    bond.shoot(800)
    felix.shoot(200)
    jason.shoot(150)
    _99.shoot(150)
    max.shoot(200)

    SecretAgent.bullets should be(res0)
  }

  /** A companion object can also see private values and variables of the instantiated objects:
    */
  def privateValuesObjects(res0: String, res1: String, res2: String, res3: String) {
    class Person(val name: String, private val superheroName: String) //The superhero name is private!

    object Person {
      def showMeInnerSecret(x: Person) = x.superheroName
    }

    val clark = new Person("Clark Kent", "Superman")
    val peter = new Person("Peter Parker", "Spiderman")
    val bruce = new Person("Bruce Wayne", "Batman")
    val diana = new Person("Diana Prince", "Wonder Woman")

    Person.showMeInnerSecret(clark) should be(res0)
    Person.showMeInnerSecret(peter) should be(res1)
    Person.showMeInnerSecret(bruce) should be(res2)
    Person.showMeInnerSecret(diana) should be(res3)
  }

}
