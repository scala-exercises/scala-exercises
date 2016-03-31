package shapelessex

import org.scalatest._
import shapeless._

/** == Coproducts and discriminated unions ==
  *
  * shapeless has a Coproduct type, a generalization of Scala's `Either` to an arbitrary number of choices. Currently it
  * exists primarily to support `Generic` (see the next section), but will be expanded analogously to `HList` in later
  * releases. Currently `Coproduct` supports mapping, selection and unification
  *
  * @param name coproducts
  */
object CoproductExercises extends FlatSpec with Matchers with exercise.Section {

  object Helper {
    type ISB = Int :+: String :+: Boolean :+: CNil

    object sizeM extends Poly1 {
      implicit def caseInt = at[Int](i ⇒ (i, i))
      implicit def caseString = at[String](s ⇒ (s, s.length))
      implicit def caseBoolean = at[Boolean](b ⇒ (b, 1))
    }

    val isb = Coproduct[ISB]("foo")
  }

  import Helper._

  /** {{{
    * type ISB = Int :+: String :+: Boolean :+: CNil
    *
    * val isb = Coproduct[ISB]("foo")
    * }}}
    */
  def selection(res0: Option[Int], res1: Option[String]) = {
    isb.select[Int] should be(res0)

    isb.select[String] should be(res1)
  }

  /** Coproduct also supports mapping given a polymorphic function such as
    * {{{
    * object sizeM extends Poly1 {
    * implicit def caseInt = at[Int](i => (i, i))
    * implicit def caseString = at[String](s => (s, s.length))
    * implicit def caseBoolean = at[Boolean](b => (b, 1))
    * }
    * }}}
    */
  def mapping(res0: Option[(String, Int)]) = {
    val m = isb map sizeM

    m.select[(String, Int)] should be(res0)
  }

  /** In the same way that adding labels To the elements of an HList gives us a record,
    * adding labels to the elements of a Coproduct gives us a discriminated union.
    */
  def unionE(res0: Option[Int], res1: Option[String], res2: Option[Boolean]) = {
    import record._, union._, syntax.singleton._

    type U = Union.`'i -> String, 's -> String, 'b -> Boolean`.T

    val u = Coproduct[U]('s ->> "foo") // Inject a String into the union at label 's

    u.get('i) should be(res0)
    u.get('s) should be(res1)
    u.get('b) should be(res2)
  }

}
