
package fail.sauce

/** Some scrapped comment */
package example

/**
  * Hello!
  * This is a comment!
  * <i>Something italic!</i>
  *
  * `Some monospaced text`
  *
  * {{{
  * scala> import my.package.complex.ComplexConversions._
  * scala> val complex = 4 + 3.i
  * complex: my.package.complex.Complex = 4 + 3i
  * }}}
  */
class Example {

  // some other comment, ignored

  /** This is an example exercise.
    * Some description here.
    */
  def validExerciseFunc1(value: String) = {
    val a = 2
    val b = 5
    val c = value.length +
      a + b


  }

  /** Take 2 */
  def validExerciseFunc2(value: String) = {

    val a = 2
    val b = 5
    val c = value.length +
      a + b

    c



  }

  /** Take 3 */
  def validExerciseFunc3(value: String) {
    val a = 2
    val b = 5
    val c = value.length +
      a + b

    c


  }


  /** This is another example exercise. */
  def validExerciseFunc4(v1: Int, v2: String) = {1; 2}


  /** Last one. */
  def validExerciseFunc5(v1: Int, v2: String) =
    v1 + v2.length

}


class ExerciseNoHeading {

}


object OhNo {}
