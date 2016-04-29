package shapelessex

import org.scalatest._
import shapeless._
import ops.hlist._

/** == Heterogenous maps ==
  *
  * Shapeless provides a heterogenous map which supports an arbitrary relation between the key type and the corresponding
  * value type,
  * {{{
  * class BiMapIS[K, V]
  * implicit val intToString = new BiMapIS[Int, String]
  * implicit val stringToInt = new BiMapIS[String, Int]
  *
  * val hm = HMap[BiMapIS](23 -> "foo", "bar" -> 13)
  * //val hm2 = HMap[BiMapIS](23 -> "foo", 23 -> 13)   // Does not compile
  * }}}
  *
  * @param name HMap
  */
object HMapExercises extends FlatSpec with Matchers with exercise.Section {

  object Helper {
    class BiMapIS[K, V]
    implicit val intToString = new BiMapIS[Int, String]
    implicit val stringToInt = new BiMapIS[String, Int]

    val hm = HMap[BiMapIS](23 → "foo", "bar" → 13)
    //val hm2 = HMap[BiMapIS](23 -> "foo", 23 -> 13)   // Does not compile
  }

  import Helper._

  /** Key/value relation to be enforced: Strings map to Ints and vice versa
    */
  def kvEnforcement(res0: Option[String], res1: Option[Int]) = {
    hm.get(23) should be(res0)
    hm.get("bar") should be(res1)
  }

  /** And in much the same way that an ordinary monomorphic Scala map can be viewed as a monomorphic function value,
    * so too can a heterogenous shapeless map be viewed as a polymorphic function value,
    */
  def mapAsPolyFValue(res0: String :: Int :: HNil) = {
    import hm._
    val l = 23 :: "bar" :: HNil
    val m = l map hm
    m should be(res0)
  }

}
