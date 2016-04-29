package shapelessex

import org.scalatest._
import shapeless._
import ops.hlist._

/** == Facilities for abstracting over arity ==
  *
  * Conversions between tuples and `HList`'s, and between ordinary Scala functions of arbitrary arity and functions which
  * take a single corresponding `HList` argument allow higher order functions to abstract over the arity of the functions
  * and values they are passed
  * {{{
  * import syntax.std.function._
  * import ops.function._
  *
  * def applyProduct[P <: Product, F, L <: HList, R](p: P)(f: F)
  * (implicit gen: Generic.Aux[P, L], fp: FnToProduct.Aux[F, L => R]) =
  * f.toProduct(gen.to(p))
  * }}}
  *
  * @param name arity
  */
object ArityExercises extends FlatSpec with Matchers with exercise.Section {

  import syntax.std.function._
  import ops.function._

  object Helper {

    def applyProduct[P <: Product, F, L <: HList, R](p: P)(f: F)(implicit gen: Generic.Aux[P, L], fp: FnToProduct.Aux[F, L ⇒ R]) =
      f.toProduct(gen.to(p))

  }

  import Helper._

  /** Abstracting over arity
    */
  def arityTest(res0: Int, res1: Int) = {
    applyProduct(1, 2)((_: Int) + (_: Int)) should be(res0)
    applyProduct(1, 2, 3)((_: Int) * (_: Int) * (_: Int)) should be(res1)
  }

}
