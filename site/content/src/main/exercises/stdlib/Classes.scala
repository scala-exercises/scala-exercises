package stdlib

import org.scalatest._

/** Classes
  *
  * Classes Description
  */
object Classes extends FlatSpec with Matchers with exercise.Section {


  /** classWithValParameterClasses
    *
    * Classes in Scala are static templates that can be instantiated into many objects at runtime.
    * Here is a class definition which defines a class Point:
    *
    * {{{
    * class Point(xc: Int, yc: Int) {
    *   var x: Int = xc
    *   var y: Int = yc
    *   def move(dx: Int, dy: Int) {
    *     x = x + dx
    *     y = y + dy
    *   }
    *   override def toString(): String = "(" + x + ", " + y + ")";
    * }
    * }}}
    * The class defines two variables `x` and `y` and two methods: `move` and `toString`.
    *
    * `move` takes two integer arguments but does not return a value (the implicit return type *Unit* corresponds to *void* in Java-like languages). `toString`, on the other hand, does not take any parameters but returns a *String* value. Since `toString` overrides the pre-defined `toString` method, it has to be tagged with the override flag.
    *
    * Classes in Scala are parameterized with constructor arguments. The code above defines two constructor arguments, `xc` and `yc`; they are both visible in the whole body of the class. In our example they are used to initialize the variables `x` and `y`.
    *
    * Classes are instantiated with the `new` primitive, as the following example will show:
    *
    * {{{
    * object Classes {
    *   def main(args: Array[String]) {
    *     val pt = new Point(1, 2)
    *     println(pt)
    *     pt.move(10, 10)
    *     println(pt)
    *   }
    * }
    * }}}
    *
    * The program defines an executable application `Classes` in form of a top-level singleton object with a `main` method. The `main` method creates a new `Point` and stores it in value `pt`. Note that values defined with the val construct are different from variables defined with the `var` construct (see class `Point` above) in that they do not allow updates; i.e. the value is constant.
    *
    * Here is the output of the program:
    *
    * {{{
    * (1, 2)
    * (11, 12)
    * }}}
    * You can define class with `var` or `val` parameters.  `val` parameters in class definition define getter:
    *
    *
    */
  def classWithValParameterClasses(res0: String) {
    class ClassWithValParameter(val name: String)
    val aClass = new ClassWithValParameter("Gandalf")
    aClass.name should be(res0)
  }

  /** classWithVarParameterClasses
    *
    * `var` parameters in class definition define getter and setter:
    */
  def classWithVarParameterClasses(res0: String, res1: String) {
    class ClassWithVarParameter(var description: String)

    val aClass = new ClassWithVarParameter("Flying character")
    aClass.description should be(res0)

    aClass.description = "Flying white character"
    aClass.description should be(res1)

    /** You can define class with private fields:
      *
      * {{{
      * class ClassWithPrivateFields(name: String)
      * val aClass = new ClassWithPrivateFields("name")
      * }}}
      * NOTE: `aClass.name` is not accessible */
  }

}