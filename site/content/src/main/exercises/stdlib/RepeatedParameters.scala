package stdlib

import stdlib.RepeatedParametersHelper._
import org.scalatest._

/** Repeated Parameters
  *
  * Repeated Parameters Description
  */
object RepeatedParameters extends FlatSpec with Matchers with exercise.Section {


  /** theLastParameterRepeatedParameters
    *
    * A repeated parameter must be the last parameter and this will let you add as many extra parameters as needed.
    *
    * Given:
    *
    * {{{
    * def repeatedParameterMethod(x: Int, y: String, z: Any*) = {
    *   "%d %ss can give you %s".format(x, y, z.mkString(", "))
    * }
    * }}}
    * Resolve:
    *
    */
  def theLastParameterRepeatedParameters(res0: String) {
    repeatedParameterMethod(3, "egg", "a delicious sandwich", "protein", "high cholesterol") should be(res0)
  }

  /** acceptCollectionRepeatedParameters
    *
    * A repeated parameter can accept a collection as the last parameter but will be considered a single object:
    */
  def acceptCollectionRepeatedParameters(res0: String) {
    repeatedParameterMethod(3, "egg", List("a delicious sandwich", "protein", "high cholesterol")) should be(res0)
  }

  /** expandCollectionRepeatedParameters
    *
    * A repeated parameter can accept a collection,and if you want it expanded, add `:_*`
    */
  def expandCollectionRepeatedParameters(res0: String) {
    repeatedParameterMethod(3, "egg", List("a delicious sandwich", "protein", "high cholesterol"): _*) should be(res0)
  }

}