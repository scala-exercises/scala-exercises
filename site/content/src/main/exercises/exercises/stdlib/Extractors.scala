package exercises.stdlib

import org.scalatest._

/** In Scala, patterns can be defined independently of case classes. To this end, a method named <code>unapply</code> is defined to yield a so-called extractor.
  *
  * For instance, the following code defines an extractor object <code>Twice</code>.
  *
  * {{{
  * object Twice {
  * def apply(x: Int): Int = x * 2
  * def unapply(z: Int): Option[Int] = if (z%2 == 0) Some(z/2) else None
  * }
  *
  * object TwiceTest extends Application {
  * val x = Twice(21)
  * x match { case Twice(n) => Console.println(n) } // prints 21
  * }
  * }}}
  *
  * There are two syntactic conventions at work here:
  *
  * - The pattern case <code>Twice(n)</code> will cause an invocation of <code>Twice.unapply</code>, which is used to match even number; the return value of the <code>unapply</code> signals whether the argument has matched or not, and any sub-values that can be used for further matching. Here, the sub-value is <code>z/2</code>
  * - The <code>apply</code> method is not necessary for pattern matching. It is only used to mimick a constructor. <code>val x = Twice(21)</code> expands to <code>val x = Twice.apply(21)</code>.
  *
  * The code in the preceding example would be expanded as follows:
  *
  * {{{
  * object TwiceTest extends Application {
  * val x = Twice.apply(21)
  * Twice.unapply(x) match { case Some(n) => Console.println(n) } // prints 21
  * }
  * }}}
  *
  * The return type of an <code>unapply</code> should be chosen as follows:
  *
  * * If it is just a test, return a <code>Boolean</code>. For instance <code>case even()</code>
  * * If it returns a single sub-value of type <code>T</code>, return a <code>Option[T]</code>
  * * If you want to return several sub-values <code>T1,...,Tn</code>, group them in an optional tuple <code>Option[(T1,...,Tn)]</code>.
  *
  * Sometimes, the number of sub-values is fixed and we would like to return a sequence. For this reason, you can also define patterns through <code>unapplySeq</code>. The last sub-value type <code>Tn</code> has to be <code>Seq[S]</code>. This mechanism is used for instance in pattern <code>case List(x1, ..., xn)</code>.
  *
  */
class Extractors extends FlatSpec with Matchers with exercise.Section {

  /** When you create a case class, it automatically can be used with pattern matching since it has an extractor:
    */
  def caseClasses(res0: String): ExerciseResult[Unit] = ExerciseRunner("Case Classes") {

    println("Inside case classes args : " + res0)

    case class Employee(firstName: String, lastName: String)

    val rob = new Employee("Robin", "Williams")
    val result = rob match {
      case Employee("Robin", _) ⇒ "Where's Batman?"
      case _                    ⇒ "No Batman Joke For You"
    }

    result should be(res0)

    println("after assert")

  }(∞)

  /** What's an extractor? In Scala it's a method in any <code>object</code> called <code>unapply</code>, and that method is used to disassemble the object given by returning a tuple wrapped in an option.
    * Extractors can be used to assign values:
    */
  def forAssigningValues(res0: String, res1: String, res2: Int, res3: Int): ExerciseResult[Unit] = ExerciseRunner("For Assigning Values") {

    class Car(val make: String, val model: String, val year: Short, val topSpeed: Short)

    object ChopShop {
      def unapply(x: Car) = Some((x.make, x.model, x.year, x.topSpeed))
    }

    val ChopShop(a, b, c, d) = new Car("Chevy", "Camaro", 1978, 120)

    a should be(res0)
    b should be(res1)
    c should be(res2)
    d should be(res3)
  }(∞)

  /** Of course an extractor can be used in pattern matching...
    */
  def patternMatching(res0: String, res1: String): ExerciseResult[Unit] = ExerciseRunner("Pattern Matching") {

    class Car(val make: String, val model: String, val year: Short, val topSpeed: Short)

    object ChopShop {
      def unapply(x: Car) = Some((x.make, x.model, x.year, x.topSpeed))
    }

    val x = new Car("Chevy", "Camaro", 1978, 120) match {
      case ChopShop(s, t, u, v) ⇒ (s, t)
      case _                    ⇒ ("Ford", "Edsel")
    }

    x._1 should be(res0)
    x._2 should be(res1)
  }(∞)

  /** Since we aren't really using u and v in the previous pattern matching with can replace them with _.
    */
  def inPatternMatchingWithWildcards(res0: String, res1: String): ExerciseResult[Unit] = ExerciseRunner("Pattern Matching Wildcards") {

    class Car(val make: String, val model: String, val year: Short, val topSpeed: Short)

    object ChopShop {
      def unapply(x: Car) = Some((x.make, x.model, x.year, x.topSpeed))
    }

    val x = new Car("Chevy", "Camaro", 1978, 120) match {
      case ChopShop(s, t, _, _) ⇒ (s, t)
      case _                    ⇒ ("Ford", "Edsel")
    }

    x._1 should be(res0)
    x._2 should be(res1)
  }(∞)

  /** As long as the method signatures aren't the same, you can have as many unapply methods as you want:
    */
  def unapplyDifferentSignature(res0: String): ExerciseResult[Unit] = ExerciseRunner("Unnaply Different Signatures") {

    class Car(val make: String, val model: String, val year: Short, val topSpeed: Short)
    class Employee(val firstName: String, val middleName: Option[String], val lastName: String)

    object Tokenizer {
      def unapply(x: Car) = Some((x.make, x.model, x.year, x.topSpeed))

      def unapply(x: Employee) = Some((x.firstName, x.lastName))
    }

    val result = new Employee("Kurt", None, "Vonnegut") match {
      case Tokenizer(c, d) ⇒ "c: %s, d: %s".format(c, d)
      case _               ⇒ "Not found"
    }

    result should be(res0)
  }(∞)

  /** An extractor can be any stable object, including instantiated classes with an unapply method.
    */
  def classesWithUnapply(res0: String): ExerciseResult[Unit] = ExerciseRunner("Classes with Unapply") {

    class Car(val make: String, val model: String, val year: Short, val topSpeed: Short) {
      def unapply(x: Car) = Some((x.make, x.model))
    }

    val camaro = new Car("Chevy", "Camaro", 1978, 122)

    val result = camaro match {
      case camaro(make, model) ⇒ "make: %s, model: %s".format(make, model)
      case _                   ⇒ "unknown"
    }

    result should be(res0)
  }(∞)

  /** What is typical is to create a custom extractor in the companion object of the class.
    * In this exercise, we use it as an assignment:
    */
  def inCompanionObject(res0: String, res1: Option[String], res2: String): ExerciseResult[Unit] = ExerciseRunner("In companion Object") {

    class Employee(
      val firstName:  String,
      val middleName: Option[String],
      val lastName:   String
    )

    object Employee {
      //factory methods, extractors, apply
      //Extractor: Create tokens that represent your object
      def unapply(x: Employee) =
        Some((x.lastName, x.middleName, x.firstName))
    }

    val singri = new Employee("Singri", None, "Keerthi")

    val Employee(a, b, c) = singri

    a should be(res0)
    b should be(res1)
    c should be(res2)
  }(∞)

  /** In this exercise we use the unapply for pattern matching employee objects
    */
  def unapplyForPatternMatching(res0: String): ExerciseResult[Unit] = ExerciseRunner("Unapply for pattern matching") {

    class Employee(
      val firstName:  String,
      val middleName: Option[String],
      val lastName:   String
    )

    object Employee {
      //factory methods, extractors, apply
      //Extractor: Create tokens that represent your object
      def unapply(x: Employee) =
        Some((x.lastName, x.middleName, x.firstName))
    }

    val singri = new Employee("Singri", None, "Keerthi")

    val result = singri match {
      case Employee("Singri", None, x)    ⇒ "Yay, Singri %s! with no middle name!".format(x)
      case Employee("Singri", Some(x), _) ⇒ "Yay, Singri with a middle name of %s".format(x)
      case _                              ⇒ "I don't care, going on break"
    }

    result should be(res0)
  }(∞)

}
