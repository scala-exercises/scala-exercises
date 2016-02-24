package stdlib

import org.scalatest._

/** traits
  *
  */
object Traits extends FlatSpec with Matchers with exercise.Section {


  /** similarToInterfacesTraits
    *
    * Similar to *interfaces* in Java, traits are used to define object types by specifying the signature of the supported methods. Unlike Java, Scala allows traits to be partially implemented; i.e. it is possible to define default implementations for some methods. In contrast to classes, traits may not have constructor parameters.
    *
    * Here is an example:
    *
    * {{{
    * trait Similarity {
    *   def isSimilar(x: Any): Boolean
    *   def isNotSimilar(x: Any): Boolean = !isSimilar(x)
    * }
    * }}}
    *
    * This trait consists of two methods `isSimilar` and `isNotSimilar`. While `isSimilar` does not provide a concrete method implementation (it is abstract in the terminology of Java), method `isNotSimilar` defines a concrete implementation. Consequently, classes that integrate this trait only have to provide a concrete implementation for `isSimilar`. The behavior for `isNotSimilar` gets inherited directly from the trait. Traits are typically integrated into a class (or other traits) with a mixin class composition:
    *
    * {{{
    * class Point(xc: Int, yc: Int) extends Similarity {
    *   var x: Int = xc
    *   var y: Int = yc
    *   def isSimilar(obj: Any) =
    *     obj.isInstanceOf[Point] &&
    *     obj.asInstanceOf[Point].x == x
    * }
    * object TraitsTest extends App {
    *   val p1 = new Point(2, 3)
    *   val p2 = new Point(2, 4)
    *   val p3 = new Point(3, 3)
    *   println(p1.isNotSimilar(p2))
    *   println(p1.isNotSimilar(p3))
    *   println(p1.isNotSimilar(2))
    * }
    * }}}
    * Here is the output of the program:
    *
    * {{{
    * false
    * true
    * true
    * }}}
    *
    * A class uses the `extends` keyword to mixin a trait if it is the only relationship the class inherits:
    */
  def similarToInterfacesTraits(res0: String) {
    case class Event(name: String)

    trait EventListener {
      def listen(event: Event): String
    }


    class MyListener extends EventListener {
      def listen(event: Event): String = {
        event match {
          case Event("Moose Stampede") => "An unfortunate moose stampede occurred"
          case _ => "Nothing of importance occurred"
        }
      }
    }

    val evt = Event("Moose Stampede")
    val myListener = new MyListener
    myListener.listen(evt) should be(res0)
  }

  /** extendsFromOneTraits
    *
    * A class can only extend from one class or trait, any subsequent extension should use the keyword `with`:
    */
  def extendsFromOneTraits(res0: String) {
    case class Event(name: String)

    trait EventListener {
      def listen(event: Event): String
    }

    class OurListener

    class MyListener extends OurListener with EventListener {
      def listen(event: Event): String = {
        event match {
          case Event("Woodchuck Stampede") => "An unfortunate woodchuck stampede occurred"
          case _ => "Nothing of importance occurred"
        }
      }
    }

    val evt = Event("Woodchuck Stampede")
    val myListener = new MyListener
    myListener.listen(evt) should be(res0)
  }

  /** polymorphicTraits
    *
    * Traits are polymorphic. Any type can be referred to by another type if related by extension:
    */
  def polymorphicTraits(res0: Boolean, res1: Boolean, res2: Boolean, res3: Boolean) {
    case class Event(name: String)

    trait EventListener {
      def listen(event: Event): String
    }

    class MyListener extends EventListener {
      def listen(event: Event): String = {
        event match {
          case Event("Moose Stampede") => "An unfortunate moose stampede occurred"
          case _ => "Nothing of importance occurred"
        }
      }
    }

    val myListener = new MyListener

    myListener.isInstanceOf[MyListener] should be(res0)
    myListener.isInstanceOf[EventListener] should be(res1)
    myListener.isInstanceOf[Any] should be(res2)
    myListener.isInstanceOf[AnyRef] should be(res3)
  }

  /** implementatedTraits
    *
    * Traits can have concrete implementations that can be mixed into concrete classes with its own state:
    */
  def implementatedTraits(res0: Int, res1: Int) {
    trait Logging {
      var logCache = List[String]()

      def log(value: String) = {
        logCache = logCache :+ value
        println(value)
      }
    }

    class Welder extends Logging {
      def weld() {
        log("welding pipe")
      }
    }

    class Baker extends Logging {
      def bake() {
        log("baking cake")
      }
    }

    val welder = new Welder
    welder.weld()


    val baker = new Baker
    baker.bake()

    welder.logCache.size should be(res0)
    baker.logCache.size should be(res1)

  }

  /** previouslyInstantiatedTraits
    *
    * Traits are instantiated before a classes instantiation:
    */
  def previouslyInstantiatedTraits(res0: String) {
    var sb = List[String]()

    trait T1 {
      sb = sb :+ "In T1: x=%s".format(x)
      val x = 1
      sb = sb :+ "In T1: x=%s".format(x)
    }

    class C1 extends T1 {
      sb = sb :+ "In C1: y=%s".format(y)
      val y = 2
      sb = sb :+ "In C1: y=%s".format(y)
    }

    sb = sb :+ "Creating C1"
    new C1
    sb = sb :+ "Created C1"

    sb.mkString(";") should be(res0)

  }

  /** fromLeftToRightTraits
    *
    * Traits are instantiated before a classes instantiation from left to right:
    */
  def fromLeftToRightTraits(res0: String) {
    var sb = List[String]()

    trait T1 {
      sb = sb :+ "In T1: x=%s".format(x)
      val x = 1
      sb = sb :+ "In T1: x=%s".format(x)
    }

    trait T2 {
      sb = sb :+ "In T2: z=%s".format(z)
      val z = 1
      sb = sb :+ "In T2: z=%s".format(z)
    }

    class C1 extends T1 with T2 {
      sb = sb :+ "In C1: y=%s".format(y)
      val y = 2
      sb = sb :+ "In C1: y=%s".format(y)
    }

    sb = sb :+ "Creating C1"
    new C1
    sb = sb :+ "Created C1"

    sb.mkString(";") should be(res0)

  }

  /** duplicateInstantiationTraits
    *
    * Instantiations are tracked and will not allow a duplicate instantiation. " + Note T1 extends T2, and C1 also extends T2, but T2 is only instantiated once:
    */
  def duplicateInstantiationTraits(res0: String) {
    var sb = List[String]()

    trait T1 extends T2 {
      sb = sb :+ "In T1: x=%s".format(x)
      val x = 1
      sb = sb :+ "In T1: x=%s".format(x)
    }

    trait T2 {
      sb = sb :+ "In T2: z=%s".format(z)
      val z = 1
      sb = sb :+ "In T2: z=%s".format(z)
    }

    class C1 extends T1 with T2 {
      sb = sb :+ "In C1: y=%s".format(y)
      val y = 2
      sb = sb :+ "In C1: y=%s".format(y)
    }

    sb = sb :+ "Creating C1"
    new C1
    sb = sb :+ "Created C1"

    sb.mkString(";") should be(res0)

  }

  /** diamondOfDeathTraits
    *
    * The diamond of [death](http://en.wikipedia.org/wiki/Diamond_problem) is avoided since instantiations are tracked and will not allow multiple instantiations:
    */
  def diamondOfDeathTraits(res0: String) {
    var sb = List[String]()

    trait T1 {
      sb = sb :+ "In T1: x=%s".format(x)
      val x = 1
      sb = sb :+ "In T1: x=%s".format(x)
    }

    trait T2 extends T1 {
      sb = sb :+ "In T2: z=%s".format(z)
      val z = 2
      sb = sb :+ "In T2: z=%s".format(z)
    }

    trait T3 extends T1 {
      sb = sb :+ "In T3: w=%s".format(w)
      val w = 3
      sb = sb :+ "In T3: w=%s".format(w)
    }

    class C1 extends T2 with T3 {
      sb = sb :+ "In C1: y=%s".format(y)
      val y = 4
      sb = sb :+ "In C1: y=%s".format(y)
    }

    sb = sb :+ "Creating C1"
    new C1
    sb = sb :+ "Created C1"

    sb.mkString(";") should be(res0)

  }

}