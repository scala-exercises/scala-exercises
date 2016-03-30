package stdlib

import org.scalatest._

/** @param name type_signatures
  */
object TypeSignatures extends FlatSpec with Matchers with exercise.Section {

  /** A method's *type signature* comprises its name, the number, order, and types of its parameters, if any, and its result type. The type signature of a class, trait, or singleton object comprises its name, the type signatures of all of its members and constructors, and its declared inheritance and mixin relations.
    *
    * In Java you declare a generic type within a `<>`, in Scala it is `[]`
    *
    * {{{
    * val z: List[String] = "Do" :: "Re" :: "Mi" :: "Fa" :: "So" :: "La" :: "Te" :: "Do" :: Nil
    * }}}
    *
    * Most of the time, Scala will infer the type and `[]` are optional:
    *
    * {{{
    * val z = "Do" :: "Re" :: "Mi" :: "Fa" :: "So" :: "La" :: "Te" :: "Do" :: Nil //Infers that the list assigned to variable is of type List[String]
    * }}}
    *
    * A trait can be declared containing a type, where a concrete implementer will satisfy the type:
    *
    */
  def typeSignatureTypeSignatures(res0: Boolean) {
    trait Randomizer[A] {
      def draw(): A
    }

    class IntRandomizer extends Randomizer[Int] {
      def draw() = {
        import util.Random
        Random.nextInt()
      }
    }

    val intRand = new IntRandomizer
    (intRand.draw < Int.MaxValue) should be(res0)
  }

  /** Class meta-information can be retrieved by class name by using `classOf[className]`
    */
  def retrieveMetaInformationTypeSignatures(res0: String, res1: String) {
    classOf[String].getCanonicalName should be(res0)
    classOf[String].getSimpleName should be(res1)
  }

  /** Class meta-information can be derived from an object reference using `getClass()`
    */
  def deriveMetaInformationTypeSignatures(res0: Boolean, res1: String, res2: String) {
    val zoom = "zoom"
    zoom.getClass should be(classOf[String])
    zoom.getClass.getCanonicalName should be(res1)
    zoom.getClass.getSimpleName should be(res2)
  }

  /** `isInstanceOf[className]` is used to determine the if an object reference is an instance of given class:
    */
  def isInstanceOfFunctionTypeSignatures(res0: Boolean) {
    trait Randomizer[A] {
      def draw(): A
    }

    class IntRandomizer extends Randomizer[Int] {
      def draw() = {
        import util.Random
        Random.nextInt()
      }
    }

    val intRand = new IntRandomizer
    intRand.draw.isInstanceOf[Int] should be(res0)
  }

  /** `asInstanceOf[className]` is used to cast one reference to another:
    */
  def asInstanceOfFunctionTypeSignatures(res0: Boolean) {
    trait Randomizer[A] {
      def draw(): A
    }

    class IntRandomizer extends Randomizer[Int] {
      def draw = {
        import util.Random
        Random.nextInt()
      }
    }

    val intRand = new IntRandomizer
    val rand = intRand
    val intRand2 = rand
    intRand2.isInstanceOf[IntRandomizer] should be(res0)
  }

  /** `asInstanceOf[className]` will throw a *ClassCastException* if a class derived from and the class target aren't from the same inheritance branch:
    *
    * {{{
    * trait Randomizer[A] {
    *  def draw(): A
    * }
    *
    * class IntRandomizer extends Randomizer[Int] {
    *  def draw() = {
    *    import util.Random
    *    Random.nextInt()
    *  }
    * }
    *
    * val intRand = new IntRandomizer
    *
    * intercept[ClassCastException] {
    *  intRand.asInstanceOf[String] //intRand cannot be cast to String
    * }
    * }}}
    *
    * `null.asInstanceOf[className]` can be used to generate basic default values
    */
  def classCastExceptionIfUncompatibleTypeSignatures(res0: Boolean, res1: Int, res2: Int) {
    null.asInstanceOf[String] == null should be(res0)
    null.asInstanceOf[Int] should be(res1)
    null.asInstanceOf[Short] should be(res2)
  }

}
