package exercises.stdlib

import org.scalatest._

/** Classes in Scala are static templates that can be instantiated into many objects at runtime. Here is a class definition which defines a class Point:
  *
  * {{{
  * class Point(xc: Int, yc: Int) {
  *   var x: Int = xc
  *   var y: Int = yc
  *
  *   def move(dx: Int, dy: Int) {
  *     x = x + dx
  *     y = y + dy
  *   }
  *
  *   override def toString(): String = "(" + x + ", " + y + ")";
  * }
  * }}}
  *
  *
  * The class defines two variables <code>x</code> and <code>y</code> and two methods: <code>move</code> and <code>toString</code>.
  *
  * <code>move</code> takes two integer arguments but does not return a value (the implicit return type <i>Unit</i> corresponds to <i>void</i> in Java-like languages). <code>toString</code>, on the other hand, does not take any parameters but returns a <i>String</i> value. Since <code>toString</code> overrides the pre-defined <code>toString</code> method, it has to be tagged with the override flag.
  *
  * Classes in Scala are parameterized with constructor arguments. The code above defines two constructor arguments, <code>xc</code> and <code>yc</code>; they are both visible in the whole body of the class. In our example they are used to initialize the variables <code>x</code> and <code>y</code>.
  *
  * Classes are instantiated with the <code>new</code> primitive, as the following example will show:
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
  * The program defines an executable application <code>Classes</code> in form of a top-level singleton object with a <code>main</code> method. The <code>main</code> method creates a new <code>Point</code> and stores it in value <code>pt</code>. Note that values defined with the val construct are different from variables defined with the <code>var</code> construct (see class <code>Point</code> above) in that they do not allow updates; i.e. the value is constant.
  *
  * Here is the output of the program:
  *
  * {{{
  * (1, 2)
  * (11, 12)
  * }}}
  */
class Classes extends FlatSpec with Matchers with exercise.Section {

  /** You can define class with <code>var</code> or <code>val</code> parameters.  <code>val</code> parameters in class definition define getter:
    *
    * {{{
    * Code example.
    * }}}
    *
    */
  def classWithValParameter(res0: String): ExerciseResult[Unit] = ExerciseRunner("Class With Val Parameter") {

    class ClassWithValParameter(val name: String)
    val aClass = new ClassWithValParameter("Gandalf")
    aClass.name should be(res0)

  }(∞)

  /** <code>var</code> parameters in class definition define getter and setter:
    */
  def classWithVarParameter(res0: String, res1: String): ExerciseResult[Unit] = ExerciseRunner("Class With Var Parameter") {

    class ClassWithVarParameter(var description: String)

    val aClass = new ClassWithVarParameter("Flying character")
    aClass.description should be(res0)

    aClass.description = "Flying white character"
    aClass.description should be(res1)

    /** You can define class with private fields:
      *
      * {{{
      * class ClassWithPrivateFields(name: String)
      * val aClass = new ClassWithPrivateFields(\"name\")
      * }}}
      *
      * NOTE: <code>aClass.name</code> is not accessible
      */
  }(∞)

}
