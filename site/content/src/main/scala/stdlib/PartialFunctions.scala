package stdlib

import org.scalatest._

/** @param name partial_functions
  *
  */
object PartialFunctions extends FlatSpec with Matchers with exercise.Section {

  /** A partial function is a `trait` that when implemented can be used as building blocks to determine a solution.  The trait `PartialFunction` requires that the method `isDefinedAt` and `apply` be implemented.
    */
  def partialFunctionPartialFunctions(res0: Int, res1: Int) {
    val doubleEvens: PartialFunction[Int, Int] = new PartialFunction[Int, Int] {
      //States that this partial function will take on the task
      def isDefinedAt(x: Int) = x % 2 == 0

      //What we do if this partial function matches
      def apply(v1: Int) = v1 * 2
    }

    val tripleOdds: PartialFunction[Int, Int] = new PartialFunction[Int, Int] {
      def isDefinedAt(x: Int) = x % 2 != 0

      def apply(v1: Int) = v1 * 3
    }

    val whatToDo = doubleEvens orElse tripleOdds //Here we chain the partial functions together

    whatToDo(3) should be(res0)
    whatToDo(4) should be(res1)
  }

  /** Case statements are a quick way to create partial functions. When you create a case statement, the `apply` and `isDefinedAt` is created for you.
    */
  def caseStatementsPartialFunctions(res0: Int, res1: Int) {
    //These case statements are called case statements with guards
    val doubleEvens: PartialFunction[Int, Int] = {
      case x if (x % 2) == 0 ⇒ x * 2
    }
    val tripleOdds: PartialFunction[Int, Int] = {
      case x if (x % 2) != 0 ⇒ x * 3
    }

    val whatToDo = doubleEvens orElse tripleOdds //Here we chain the partial functions together
    whatToDo(3) should be(res0)
    whatToDo(4) should be(res1)
  }

  /** The result of partial functions can have an `andThen` function added to the end of the chain
    */
  def andThenPartialFunctions(res0: Int, res1: Int) {
    //These are called case statements with guards
    val doubleEvens: PartialFunction[Int, Int] = {
      case x if (x % 2) == 0 ⇒ x * 2
    }
    val tripleOdds: PartialFunction[Int, Int] = {
      case x if (x % 2) != 0 ⇒ x * 3
    }

    val addFive = (x: Int) ⇒ x + 5
    val whatToDo = doubleEvens orElse tripleOdds andThen addFive //Here we chain the partial functions together
    whatToDo(3) should be(res0)
    whatToDo(4) should be(res1)
  }

  /** The result of partial functions can have an `andThen` function added to the end of the chain used to continue onto another chain of logic:
    */
  def chainOfLogicPartialFunctions(res0: String, res1: String) {
    val doubleEvens: PartialFunction[Int, Int] = {
      case x if (x % 2) == 0 ⇒ x * 2
    }
    val tripleOdds: PartialFunction[Int, Int] = {
      case x if (x % 2) != 0 ⇒ x * 3
    }

    val printEven: PartialFunction[Int, String] = {
      case x if (x % 2) == 0 ⇒ "Even"
    }
    val printOdd: PartialFunction[Int, String] = {
      case x if (x % 2) != 0 ⇒ "Odd"
    }

    val whatToDo = doubleEvens orElse tripleOdds andThen (printEven orElse printOdd)

    whatToDo(3) should be(res0)
    whatToDo(4) should be(res1)
  }

}
