package stdlib

import org.scalatest._

/** @param name higher_order_functions
  *
  */
object HigherOrderFunctions extends FlatSpec with Matchers with exercise.Section {


  /** Meet lambda. Scala provides a relatively lightweight syntax for defining anonymous functions. Anonymous functions in source code are called function literals and at run time, function literals are instantiated into objects called function values.
    *
    * Scala supports first-class functions, which means you can express functions in function literal syntax, i.e.,` (x: Int) => x + 1`, and that functions can be represented by objects, which are called function values.
    */
  def meetLamdaHigherOrderFunctions(res0: Int, res1: Int, res2: Int, res3: Int, res4: Int, res5: Int) {
    def lambda = { x: Int => x + 1 }
    def lambda2 = (x: Int) => x + 1
    val lambda3 = (x: Int) => x + 1

    val lambda4 = new Function1[Int, Int] {
      def apply(v1: Int): Int = v1 + 1
    }

    def lambda5(x: Int) = x + 1

    val result = lambda(3)
    val `result1andhalf` = lambda.apply(3)

    val result2 = lambda2(3)
    val result3 = lambda3(3)
    val result4 = lambda4(3)
    val result5 = lambda5(3)

    result should be(res0)
    result1andhalf should be(res1)
    result2 should be(res2)
    result3 should be(res3)
    result4 should be(res4)
    result5 should be(res5)
  }

  /** An anonymous function can also take on a different look by taking out the brackets
    */
  def differentLookHigherOrderFunctions(res0: Int) {
    def lambda = (x: Int) => x + 1
    def result = lambda(5)
    result should be(res0)
  }

  /** {{{
    * val multiplier = (i:Int) => i * 10
    * }}}
    *
    * Here the only variable used in the function body, `i * 10`, is `i`, which is defined as a parameter to the function.
    */
  def meetClosureHigherOrderFunctions(res0: Int, res1: Int) {
    var incrementer = 1

    def closure = {
      x: Int => x + incrementer
    }

    val result1 = closure(10)
    result1 should be(res0)

    incrementer = 2

    val result2 = closure(10)
    result2 should be(res1)
  }

  /** We can take that closure and throw into a method and it will still hold the environment
    */
  def holdEnvironmentHigherOrderFunctions(res0: Int, res1: Int) {
    def summation(x: Int, y: Int => Int) = y(x)

    var incrementer = 3
    def closure = (x: Int) => x + incrementer

    val result = summation(10, closure)
    result should be(res0)

    incrementer = 4
    val result2 = summation(10, closure)
    result2 should be(res1)
  }

  /** Function returning another function:
    */
  def returningFunctionHigherOrderFunctions(res0: Boolean, res1: Int, res2: Int) {
    def addWithoutSyntaxSugar(x: Int) = {
      new Function1[Int, Int]() {
        def apply(y: Int): Int = x + y
      }
    }
    addWithoutSyntaxSugar(1).
        isInstanceOf[Function1[Int, Int]] should be(res0)

    addWithoutSyntaxSugar(2)(3) should be(res1)

    def fiveAdder = addWithoutSyntaxSugar(5)
    fiveAdder(5) should be(res2)
  }

  /** Function returning another function using an anonymous function:
    */
  def returningAnonymousFunctionHigherOrderFunctions(res0: Boolean, res1: Int, res2: Int) {
    def addWithSyntaxSugar(x: Int) = (y: Int) => x + y

    addWithSyntaxSugar(1).isInstanceOf[Function1[Int, Int]] should be(res0)
    addWithSyntaxSugar(2)(3) should be(res1)

    def fiveAdder = addWithSyntaxSugar(5)
    fiveAdder(5) should be(res2)
  }

  /** `isInstanceOf` is the same as `instanceof` in java, but in this case the parameter types can be *blanked out* using existential types with a single underline, since parameter type are unknown at runtime.
    */
  def isInstanceOfMethodHigherOrderFunctions(res0: Boolean) {
    def addWithSyntaxSugar(x: Int) = (y: Int) => x + y

    addWithSyntaxSugar(1).isInstanceOf[Function1[_, _]] should be(res0)
  }

  /** Function taking another function as parameter. Helps in composing functions.
    *
    * Hint: a map method applies the function to each element of a list
    */
  def functionAsParameterHigherOrderFunctions(res0: String, res1: String, res2: String) {
    def makeUpper(xs: List[String]) = xs map {
      _.toUpperCase
    }

    def makeWhatEverYouLike(xs: List[String], sideEffect: String => String) = {
      xs map sideEffect
    }

    makeUpper(List("abc", "xyz", "123")) should be(res0)

    makeWhatEverYouLike(List("ABC", "XYZ", "123"), {
      x => x.toLowerCase
    }) should be(res1)

    //using it inline
    List("Scala", "Erlang", "Clojure") map {
      _.length
    } should be(res2)
  }

}
