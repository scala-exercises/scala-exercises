package stdlib

import stdlib.NamedandDefaultArgumentsHelper._
import org.scalatest._

/** Named and Default Arguments
  *
  * Named and Default Arguments Description
  */
object NamedandDefaultArguments extends FlatSpec with Matchers with exercise.Section {


  /** classWithoutParametersNamedandDefaultArguments
    *
    * When calling methods and functions, you can use the name of the variables explicitly in the call, like so:
    *
    * {{{
    * def printName(first:String, last:String) = {
    *     println(first + " " + last)
    * }
    *
    * printName("John","Smith") // Prints "John Smith"
    * printName(first = "John",last = "Smith") // Prints "John Smith"
    * printName(last = "Smith",first = "John") // Prints "John Smith"
    * }}}
    *
    * Note that once you are using parameter names in your calls, the order doesn't matter, so long as all parameters are named. This feature works well with default parameter values:
    *
    * {{{
    * def printName(first:String = "John", last:String = "Smith") = {
    *     println(first + " " + last)
    * }
    * printName(last = "Jones") // Prints "John Jones"
    * }}}
    *
    * Given classes below:
    *
    * {{{
    * class WithoutClassParameters() {
    *   def addColors(red: Int, green: Int, blue: Int) = {
    *     (red, green, blue)
    *   }
    *
    *   def addColorsWithDefaults(red: Int = 0, green: Int = 0, blue: Int = 0) = {
    *     (red, green, blue)
    *   }
    * }
    *
    * class WithClassParameters(val defaultRed: Int, val defaultGreen: Int, val defaultBlue: Int) {
    *   def addColors(red: Int, green: Int, blue: Int) = {
    *     (red + defaultRed, green + defaultGreen, blue + defaultBlue)
    *   }
    *
    *   def addColorsWithDefaults(red: Int = 0, green: Int = 0, blue: Int = 0) = {
    *     (red + defaultRed, green + defaultGreen, blue + defaultBlue)
    *   }
    * }
    *
    * class WithClassParametersInClassDefinition(val defaultRed: Int = 0, val defaultGreen: Int = 255, val defaultBlue: Int = 100) {
    *   def addColors(red: Int, green: Int, blue: Int) = {
    *     (red + defaultRed, green + defaultGreen, blue + defaultBlue)
    *   }
    *
    *   def addColorsWithDefaults(red: Int = 0, green: Int = 0, blue: Int = 0) = {
    *     (red + defaultRed, green + defaultGreen, blue + defaultBlue)
    *   }
    * }
    *
    * }}}
    *
    * Can specify arguments in any order if you use their names:
    */
  def classWithoutParametersNamedandDefaultArguments(res0: String, res1: String, res2: String) {
    val me = new WithoutClassParameters()

    // what happens if you change the order of these parameters (nothing)
    val myColor = me.addColors(green = 0, red = 255, blue = 0)

    // for koan, remove the values in the should equal
    myColor should equal(res0, res1, res2)
  }

  /** defaultArgumentsNamedandDefaultArguments
    *
    * Can default arguments if you leave them off:
    */
  def defaultArgumentsNamedandDefaultArguments(res0: String, res1: String, res2: String) {
    val me = new WithoutClassParameters()
    val myColor = me.addColorsWithDefaults(green = 255)

    myColor should equal(res0, res1, res2)
  }

  /** anyOrderNamedandDefaultArguments
    *
    * Can access class parameters and specify arguments in any order if you use their names:
    */
  def anyOrderNamedandDefaultArguments(res0: String, res1: String, res2: String) {
    val me = new WithClassParameters(40, 50, 60)
    val myColor = me.addColors(green = 50, red = 60, blue = 40)

    myColor should equal(res0, res1, res2)
  }

  /** accessClassParametersNamedandDefaultArguments
    *
    * Can access class parameters and default arguments if you leave them off
    */
  def accessClassParametersNamedandDefaultArguments(res0: String, res1: String, res2: String) {
    val me = new WithClassParameters(10, 20, 30)
    val myColor = me.addColorsWithDefaults(green = 70)

    myColor should equal(res0, res1, res2)
  }

  /** defaultClassArgumentsNamedandDefaultArguments
    *
    * Can default class parameters and have default arguments too
    */
  def defaultClassArgumentsNamedandDefaultArguments(res0: String, res1: String, res2: String) {
    val me = new WithClassParametersInClassDefinition()
    val myColor = me.addColorsWithDefaults(green = 70)

    myColor should equal(res0, res1, res2)
  }

  /** functionalDefaulParametersNamedandDefaultArguments
    *
    * Default parameters can be functional too
    */
  def functionalDefaulParametersNamedandDefaultArguments(res0: String, res1: String) {
    def reduce(a: Int, f: (Int, Int) => Int = _ + _): Int = f(a, a)

    reduce(5) should equal(res0)
    reduce(5, _ * _) should equal(res1)
  }

}