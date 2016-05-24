package stdlib

import org.scalatest._

/** @param name pattern_matching
  *
  */
object PatternMatching extends FlatSpec with Matchers with exercise.Section {

  /** Scala has a built-in general pattern matching mechanism. It allows to match on any sort of data with a first-match policy.
    * Here is a small example which shows how to match against an integer value:
    *
    * {{{
    * object MatchTest1 extends App {
    * def matchTest(x: Int): String = x match {
    * case 1 => "one"
    * case 2 => "two"
    * case _ => "many"
    * }
    * println(matchTest(3))
    * }
    * }}}
    *
    * The block with the `case` statements defines a function which maps integers to strings. The `match` keyword provides a convenient way of applying a function (like the pattern matching function above) to an object.
    *
    * Scala's pattern matching statement is most useful for matching on algebraic types expressed via `case classes`.
    * Scala also allows the definition of patterns independently of case classes, using `unapply` methods in extractor objects.
    *
    * Pattern matching returns something:
    */
  def patternMatchingMechanismPatternMatching(res0: Int) {
    val stuff = "blue"

    val myStuff = stuff match {
      case "red" ⇒
        println("RED"); 1
      case "blue" ⇒
        println("BLUE"); 2
      case "green" ⇒
        println("GREEN"); 3
      case _ ⇒ println(stuff); 0 //case _ will trigger if all other cases fail.
    }

    myStuff should be(res0)
  }

  /** Pattern matching can return complex somethings:
    */
  def returnComplexPatternMatching(res0: Int, res1: Int, res2: Int) {
    val stuff = "blue"

    val myStuff = stuff match {
      case "red"   ⇒ (255, 0, 0)
      case "green" ⇒ (0, 255, 0)
      case "blue"  ⇒ (0, 0, 255)
      case _       ⇒ println(stuff); 0
    }

    myStuff should be(res0, res1, res2)
  }

  /** Pattern matching can match complex expressions:
    */
  def complexExpressionsPatternMatching(res0: String) {
    def goldilocks(expr: Any) = expr match {
      case ("porridge", "Papa") ⇒ "Papa eating porridge"
      case ("porridge", "Mama") ⇒ "Mama eating porridge"
      case ("porridge", "Baby") ⇒ "Baby eating porridge"
      case _                    ⇒ "what?"
    }

    goldilocks(("porridge", "Mama")) should be(res0)
  }

  /** Pattern matching can wildcard parts of expressions:
    */
  def wildcardParsPatternMatching(res0: String, res1: String) {
    def goldilocks(expr: Any) = expr match {
      case ("porridge", _)   ⇒ "eating"
      case ("chair", "Mama") ⇒ "sitting"
      case ("bed", "Baby")   ⇒ "sleeping"
      case _                 ⇒ "what?"
    }

    goldilocks(("porridge", "Papa")) should be(res0)
    goldilocks(("chair", "Mama")) should be(res1)
  }

  /** Pattern matching can substitute parts of expressions:
    */
  def substitutePartsPatternMatching(res0: String, res1: String) {
    def goldilocks(expr: Any) = expr match {
      case ("porridge", bear) ⇒ bear + " said someone's been eating my porridge"
      case ("chair", bear)    ⇒ bear + " said someone's been sitting in my chair"
      case ("bed", bear)      ⇒ bear + " said someone's been sleeping in my bed"
      case _                  ⇒ "what?"
    }

    goldilocks(("porridge", "Papa")) should be(res0)
    goldilocks(("chair", "Mama")) should be(res1)
  }

  //TODO: Improve compiler to ignore regular expressions in the body of the exercise
  //  /** regularExpressionsPatternMatching
  //    *
  //    * Pattern matching can be done on regular expression groups:
  //    */
  //  def regularExpressionsPatternMatching(res0: String, res1: String) {
  //    val EatingRegularExpression = """Eating Alert: bear=([^,]+),\s+source=(.+)""".r //.r turns a String to a regular expression
  //    val SittingRegularExpression =
  //      """Sitting Alert: bear=([^,]+),\s+source=(.+)""".r
  //    val SleepingRegularExpression = """Sleeping Alert: bear=([^,]+),\s+source=(.+)""".r
  //
  //    def goldilocks(expr: String) = expr match {
  //      case (EatingRegularExpression(bear, source)) => "%s said someone's been eating my %s".format(bear, source)
  //      case (SittingRegularExpression(bear, source)) => "%s said someone's been sitting on my %s".format(bear, source)
  //      case (SleepingRegularExpression(bear, source)) => "%s said someone's been sleeping in my %s".format(bear, source)
  //      case _ => "what?"
  //    }
  //
  //    goldilocks("Eating Alert: bear=Papa, source=porridge") should be(res0)
  //    goldilocks("Sitting Alert: bear=Mama, source=chair") should be(res1)
  //  }

  /** A backquote can be used to refer to a stable variable in scope to create a case statement. This prevents what is called "Variable Shadowing"
    */
  def createCaseStatementPatternMatching(res0: String, res1: String, res2: String, res3: String) {
    val foodItem = "porridge"

    def goldilocks(expr: Any) = expr match {
      case (`foodItem`, _)   ⇒ "eating"
      case ("chair", "Mama") ⇒ "sitting"
      case ("bed", "Baby")   ⇒ "sleeping"
      case _                 ⇒ "what?"
    }

    goldilocks(("porridge", "Papa")) should be(res0)
    goldilocks(("chair", "Mama")) should be(res1)
    goldilocks(("porridge", "Cousin")) should be(res2)
    goldilocks(("beer", "Cousin")) should be(res3)
  }

  /** A backquote can be used to refer to a method parameter as a stable variable to create a case statement:
    */
  def stableVariablePatternMatching(res0: Boolean, res1: Boolean, res2: Boolean) {
    def patternEquals(i: Int, j: Int) = j match {
      case `i` ⇒ true
      case _   ⇒ false
    }
    patternEquals(3, 3) should be(res0)
    patternEquals(7, 9) should be(res1)
    patternEquals(9, 9) should be(res2)
  }

  /** To pattern match against a `List`, the list can be broken out into parts, in this case the head `x` and the tail `xs`. Since the case doesn't terminate in `Nil`, `xs` is interpreted as the rest of the list:
    */
  def againstListsPatternMatching(res0: Int) {
    val secondElement = List(1, 2, 3) match {
      case x :: xs ⇒ xs.head
      case _       ⇒ 0
    }

    secondElement should be(res0)
  }

  /** To obtain the second element you can expand on the pattern. Where `x` is the first element, `y` is the second element, and `xs` is the rest:
    */
  def againstListsIIPatternMatching(res0: Int) {
    val secondElement = List(1, 2, 3) match {
      case x :: y :: xs ⇒ y
      case _            ⇒ 0
    }

    secondElement should be(res0)
  }

  /** Same koan as above, but we are pattern matching a list with only one item!
    */
  def againstListsIIIPatternMatching(res0: Int) {
    val secondElement = List(1) match {
      case x :: y :: xs ⇒ y
      case _            ⇒ 0
    }

    secondElement should be(res0)
  }

  /** To pattern match against `List`, you can also establish a pattern match if you know the exact number of elements in a `List`:
    */
  def againstListsIVPatternMatching(res0: Int) {
    val r = List(1, 2, 3) match {
      case x :: y :: Nil ⇒ y
      case _             ⇒ 0
    }

    r should be(res0)
  }

}
