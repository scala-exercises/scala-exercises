package shapelessex

import org.scalatest._
import shapeless._

/** == Singleton-typed literals ==
  *
  * Although Scala's typechecker has always represented singleton types for literal values internally, there has not
  * previously been syntax available to express them, other than by [modifying the compiler][literaltype]. shapeless adds
  * support for singleton-typed literals via implicit macros.
  *
  * @param name singletons_literals
  *
  */
object SingletonExercises extends FlatSpec with Matchers with exercise.Section {

  /** Singleton types bridge the gap between the value level and the type level and hence allow the exploration in Scala
    * of techniques which would typically only be available in languages with support for full-spectrum dependent types.
    * The latest iteration of shapeless records makes a start on that.
    * Another simpler application is the use of Int literals to index into HLists and tuples,
    */
  def indexHListAndTuples(res0: String, res1: String) = {
    import syntax.std.tuple._

    val l = 23 :: "foo" :: true :: HNil
    l(1) should be(res0)

    val t = (23, "foo", true)
    t(1) should be(res1)
  }

  import shapeless._, syntax.singleton._

  /** The examples in the shapeless tests and the following illustrate other possibilities,
    * {{{
    * import shapeless._, syntax.singleton._
    * }}}
    */
  def narrow1(res0: Witness) = {
    res0.value == 23 should be(true)
  }

  /**
    */
  def narrow2(res0: Witness) = {
    res0.value == "foo" should be(true)
  }

  /**
    */
  def select(res0: Int, res1: String) = {
    val (wTrue, wFalse) = (Witness(true), Witness(false))

    type True = wTrue.T
    type False = wFalse.T

    trait Select[B] { type Out }

    implicit val selInt = new Select[True] { type Out = Int }
    implicit val selString = new Select[False] { type Out = String }

    def select(b: WitnessWith[Select])(t: b.instance.Out) = t

    select(true)(23) should be(res0)

    //select(true)("foo")
    //error: type mismatch;
    // found   : String("foo")
    // required: Int
    //              select(true)("foo")
    //                           ^

    //select(false)(23)
    // error: type mismatch;
    //found   : Int(23)
    //required: String

    select(false)("foo") should be(res1)
  }

}
