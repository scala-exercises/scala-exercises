package stdlib

import org.scalatest._

/** parent_classes
  *
  */
object ParentClasses extends FlatSpec with Matchers with exercise.Section {


  /** In contrast to Java, all values in Scala are objects (including numerical values and functions). Since Scala is class-based, all values are instances of a class.
    *
    * Class hierarchy is linear, a class can only extend from one parent class:
    *
    */
  def allValuesAreObjectsParentClasses(res0: String, res1: String) {
    class Soldier(val firstName: String, val lastName: String) {}
    class Pilot(override val firstName: String, override val lastName: String,
        val squadron: Long) extends Soldier(firstName, lastName)
    val pilot = new Pilot("John", "Yossarian", 256)
    pilot.firstName should be(res0)
    pilot.lastName should be(res1)
  }

  /** A class that extends from another is polymorphic:
    */
  def polymorphicParentClasses(res0: String, res1: String) {
    class Soldier(val firstName: String, val lastName: String) {}
    class Pilot(override val firstName: String, override val lastName: String,
        val squadron: Long) extends Soldier(firstName, lastName)

    val pilot = new Pilot("John", "Yossarian", 256)
    val soldier: Soldier = pilot

    soldier.firstName should be(res0)
    soldier.lastName should be(res1)
  }

  /** An abstract class, as in Java, cannot be instantiated and only inherited:
    *
    * {{{
    * abstract class Soldier(val firstName: String, val lastName: String) {}
    *
    * // if you uncomment this line, if will fail compilation
    * //val soldier = new Soldier
    * }}}
    *
    * A class can be placed inside an abstract class just like in java:
    */
  def abstractClassParentClasses(res0: Int) {
    abstract class Soldier(val firstName: String, val lastName: String) {

      class Catch(val number: Long) {
        // nothing to do here.  Just observe that it compiles
      }

    }
    class Pilot(override val firstName: String, override val lastName: String,
        val squadron: Long) extends Soldier(firstName, lastName)

    val pilot = new Pilot("John", "Yossarian", 256)
    val catchNo = new pilot.Catch(22) //using the pilot instance's path, create an catch object for it.
    catchNo.number should be(res0)
  }

}
