package stdlib

import TypeVarianceHelper._
import org.scalatest._

/** @param name type_variance
  */
object TypeVariance extends FlatSpec with Matchers with exercise.Section {

  /** A traditional objection to static typing is that it has much syntactic overhead. Scala alleviates this by providing *type inference*.
    *
    * The classic method for type inference in functional programming languages is *Hindley-Milner*, and it was first employed in ML.
    *
    * Scala's type inference system works a little differently, but it's similar in spirit: infer constraints, and attempt to unify a type.
    *
    * Using type inference the type that you instantiate it will be the val or var reference type:
    */
  def syntacticOverheadTypeVariance(res0: String) {
    class MyContainer[A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] var item = a

      def get = item

      def set(a: A) {
        item = a
      }

      def contents = manifest.runtimeClass.getSimpleName
    }

    val fruitBasket = new MyContainer(new Orange())
    fruitBasket.contents should be(res0)
  }

  /** You can explicitly declare the type variable of the object during instantiation:
    */
  def typeVariableTypeVariance(res0: String) {
    class MyContainer[A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] var item = a

      def get = item

      def set(a: A) {
        item = a
      }

      def contents = manifest.runtimeClass.getSimpleName
    }

    val fruitBasket = new MyContainer[Fruit](new Orange())
    fruitBasket.contents should be(res0)
  }

  /** You can coerce your object to a type.
    */
  def coerceObjectTypeVariance(res0: String) {
    class MyContainer[A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] var item = a

      def get = item

      def set(a: A) {
        item = a
      }

      def contents = manifest.runtimeClass.getSimpleName
    }

    val fruitBasket: MyContainer[Fruit] = new MyContainer(new Orange())
    fruitBasket.contents should be(res0)
  }

  /** Scala's type system has to account for class hierarchies together with polymorphism. Class hierarchies allow the expression of subtype relationships. A central question that comes up when mixing OO with polymorphism is: if `T'` is a subclass of `T`, is `Container[T']` considered a subclass of `Container[T]`? Variance annotations allow you to express the following relationships between class hierarchies & polymorphic types:
    *
    * ####Covariant:
    * - `C[T']` is a subclass of `C[T]`
    * - Scala notation: `[+T]`
    *
    * ####Contravariant:
    * - `C[T]` is a subclass of `C[T']`
    * - Scala notation: `[-T]`
    *
    * ####Invariant:
    * - `C[T]` and `C[T']` are not related
    * - Scala notation: `[T]`
    *
    *
    * That one probably blew your mind. Now if you assign a type to the instantiation that is different to the variable type, you'll have problems. You may want to take time after this koan to compare and contrast with the previous one.
    *
    * {{{
    * class MyContainer[A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
    * private[this] var item = a
    *
    * def get = item
    *
    * def set(a: A) {
    *  item = a
    * }
    *
    * def contents = manifest.runtimeClass.getSimpleName
    * }
    *
    * // Uncomment the following line
    * // val fruitBasket:MyContainer[Fruit] = new MyContainer[Orange](new Orange())
    * }}}
    *
    * So, how do we get to set a Fruit basket to an Orange basket? You make it covariant using `+`. This will allow you to set the container to either a variable with the same type or parent type. In other words, you can assign `MyContainer[Fruit]` or `MyContainer[Citrus]`.
    */
  def polymorphismTypeVariance(res0: String) {
    class MyContainer[+A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] val item = a

      def get = item

      def contents = manifest.runtimeClass.getSimpleName
    }

    val fruitBasket: MyContainer[Fruit] = new MyContainer[Orange](new Orange())
    fruitBasket.contents should be(res0)
  }

  /** The problem with covariance is that you can't mutate, set or change the object since it has to guarantee that what you put into it is a valid type.  In other words the reference is a fruit basket, but we still have to make sure that no other fruit can be placed in our orange basket:
    */
  def covarianceInmutableTypeVariance(res0: String) {
    class MyContainer[+A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] val item = a

      def get = item

      def contents = manifest.runtimeClass.getSimpleName
    }

    val fruitBasket: MyContainer[Fruit] = new MyContainer[Orange](new Orange())
    fruitBasket.contents should be(res0)

    class NavelOrange extends Orange //Creating a subtype to prove a point
    //val navelOrangeBasket: MyContainer[NavelOrange] = new MyContainer[Orange](new Orange()) //Bad!
    //val tangeloBasket: MyContainer[Tangelo] = new MyContainer[Orange](new Orange()) //Bad!
  }

  /** Declaring - indicates contravariance variance.  Using - you can apply any container with a certain type to a container with a superclass of that type.  This is reverse to covariant.  In our example, we can set a citrus basket to an orange or tangelo basket. Since an orange or tangelo basket are a citrus basket. Contravariance is the opposite of covariance:
    */
  def contravarianceVarianceTypeVariance(res0: String, res1: String, res2: String, res3: String) {
    class MyContainer[-A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] var item = a

      def set(a: A) {
        item = a
      }

      def contents = manifest.runtimeClass.getSimpleName
    }

    val citrusBasket: MyContainer[Citrus] = new MyContainer[Citrus](new Orange)
    citrusBasket.contents should be(res0)
    val orangeBasket: MyContainer[Orange] = new MyContainer[Citrus](new Tangelo)
    orangeBasket.contents should be(res1)
    val tangeloBasket: MyContainer[Tangelo] = new MyContainer[Citrus](new Orange)
    tangeloBasket.contents should be(res2)

    val orangeBasketReally: MyContainer[Orange] = tangeloBasket.asInstanceOf[MyContainer[Orange]]
    orangeBasketReally.contents should be(res3)
    orangeBasketReally.set(new Orange())
  }

  /** Declaring contravariance variance with `-` also means that the container cannot be accessed with a getter or some other accessor, since that would cause type inconsistency.  In our example, you can put an orange or a tangelo into a citrus basket. Problem is, if you have a reference to an orange basket, and if you believe that you have an orange basket then you shouldn't expect to get a tangelo out of it. A reference to a parent type means you cannot anticipate getting a more specific type:
    */
  def contravarianceWithoutGetterTypeVariance(res0: String, res1: String, res2: String) {
    class MyContainer[-A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] var item = a

      def set(a: A) {
        item = a
      }

      def contents = manifest.runtimeClass.getSimpleName
    }

    val citrusBasket: MyContainer[Citrus] = new MyContainer[Citrus](new Orange)
    citrusBasket.contents should be(res0)
    val orangeBasket: MyContainer[Orange] = new MyContainer[Citrus](new Tangelo)
    orangeBasket.contents should be(res1)
    val tangeloBasket: MyContainer[Tangelo] = new MyContainer[Citrus](new Orange)
    tangeloBasket.contents should be(res2)
  }

  /** Declaring neither `-`/`+`, indicates invariance variance. You cannot use a superclass variable reference (\"contravariant\" position) or a subclass variable reference (\"covariant\" position) of that type.  In our example, this means that if you create a citrus basket you can only reference that citrus basket with a citrus variable only.
    *
    * Invariance means you need to specify the type exactly:
    */
  def invarianceVarianceTypeVariance(res0: String) {
    class MyContainer[A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] var item = a

      def set(a: A) {
        item = a
      }

      def get = item

      def contents = manifest.runtimeClass.getSimpleName
    }

    val citrusBasket: MyContainer[Citrus] = new MyContainer[Citrus](new Orange)
    citrusBasket.contents should be(res0)
  }

  /** Declaring a type as invariant also means that you can both mutate and access elements from an object of generic type:
    */
  def invariantTypeVariance(res0: String, res1: String) {
    class MyContainer[A](a: A)(implicit manifest: scala.reflect.Manifest[A]) {
      private[this] var item = a

      def set(a: A) {
        item = a
      }

      def get = item

      def contents = manifest.runtimeClass.getSimpleName
    }

    val citrusBasket: MyContainer[Citrus] = new MyContainer[Citrus](new Orange)

    citrusBasket.set(new Orange)
    citrusBasket.contents should be(res0)

    citrusBasket.set(new Tangelo)
    citrusBasket.contents should be(res1)
  }

}
