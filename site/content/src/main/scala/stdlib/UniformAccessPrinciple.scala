package stdlib

import org.scalatest._

/** @param name uniform_access_principle
  *
  */
object UniformAccessPrinciple extends FlatSpec with Matchers with exercise.Section {

  /** The Scala language implements a programming concept known as the [Uniform Access Principle](http://en.wikipedia.org/wiki/Uniform_access_principle) which was first put forth by Bertrand Meyer, inventor of the Eiffel programming language.
    *
    * This principle states that variables and parameterless functions should be accessed using the same syntax. Scala supports this principle by not allowing parentheses to be placed at call sites of parameterless functions. As a result, a parameterless function definition can be changed to a val, or vice versa, without affecting client code.
    *
    */
  def uniformAccessPrincipleUniformAccessPrinciple(res0: Int, res1: Int) {
    class Test1(val age: Int = 10)
    class Test2(_age: Int) {
      def age: Int = _age
    }

    new Test1(10).age should be(res0)
    new Test2(11).age should be(res1)
  }

}
